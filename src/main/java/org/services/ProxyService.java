package org.services;

import org.ServiceModel.ElementHolder;
import org.baseFilters.BaseFilterModel;
import org.baseFilters.BaseFilterModelAnnotation;
import org.baseModels.BaseDataModel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
Proxy Service handles all action about controlling event chain, output, input, task pool, inject filtering, loading model and filter module dynamic
Event Chain: [INIT->OUTPUT_START->TASK_START->INIT_FILTER->INPUT_START] ->
             [Monitor Pipeline] ->
             [INPUT_END -> QUEUE_EMPTY -> END_FILTER -> OUTPUT_END -> DESTROY -> FINISH]

I write class injector because, dynamic load filterModel and dataModel
So any developer just could write their dataModel and FilterModel and compile that then loaded in this service ;) easy
 */

public class ProxyService implements Runnable {
    private final static Integer fixThreadPoolSize = 10; // The Fixed Size Of Pool
    private final int sleepProxyServiceTime = 500;
    private STATE proxyServiceState;
    private Integer sizeOfPool = -1; // The Real Size Of Pool
    private ArrayList<ProcessingTask> processingTasksList; // Pointer To Tasks
    private ExecutorService executePool; // Execute Service Pool
    private ConcurrentLinkedQueue<ElementHolder> primaryStorage; // The Main Storage Queue For Lines
    private ByteBuffer outPutChannelBuffer; // OutPut Buffer
    private String repositoryModel; // location for load models
    private String filterModel, dataModel;
    private String inputAddress;
    private String outputAddress;
    private int outputBufferSize;
    private int waterMark;
    private OutputIO outputIO; // Left Hand Of Proxy , Output
    private InputIO inputIO; // Right Hand Of Proxy, Input
    private FilterApplicator<BaseFilterModel> filterApplicator; // Filter Applicator , this applicator just used for init,end filters
    // We could write better Performance Algorithm //
    // But for now this is enough what we see on last size and take difference with now and take decision //
    // You could write any idea for taking snapshot and controlling on In memory storage
    private int slowPerformanceTaskIndicator = 50000; // slow performance indicator
    private int slowPerformanceSeenHighRisk = 5;
    private int counterSlowPerformanceSeen = 0;
    private int lastSeenSize = 0;
    public ProxyService(String repositoryModel, String filterModel, String dataModel
            , String inputAddress, String outputAddress,
                        int outputBufferSize, int waterMark, int inputSizePool) {
        this.repositoryModel = repositoryModel;
        this.filterModel = filterModel;
        this.dataModel = dataModel;
        this.sizeOfPool = inputSizePool;
        this.inputAddress = inputAddress;
        this.outputAddress = outputAddress;
        this.outputBufferSize = outputBufferSize;
        this.waterMark = waterMark;

    }

    public void init() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, FileNotFoundException, InterruptedException, MalformedURLException, ClassNotFoundException {

        // Queue Dynamically Increased //
        // More Queue Cap Better Performance Because Of (Queue Full Exception On Producer) & (Take Function On Consumer) //
        primaryStorage = new ConcurrentLinkedQueue<>();

        // Dynamically Increase Task To Assign To Pool With Queue Items Metric [Exception Queue Full] //
        // Per Any Of Queue Full Exception Algorithm Asking Another Task To Pool //
        // Zero Day create 1/3 of pool and pool size is fixed variable but user can increase with input argument: fix * userInput //

        sizeOfPool = fixThreadPoolSize * sizeOfPool; // real pool size is fix*(user request metric increase) , ex: 10*2
        executePool = Executors.newFixedThreadPool(sizeOfPool);
        processingTasksList = new ArrayList<>();

        // Output Controller //
        outputIO = new OutputIO(outputAddress, outputBufferSize, waterMark, this);

        // Input Controller //
        inputIO = new InputIO(inputAddress, this);

        // Predefine Applicator //
        filterApplicator = createApplicator();


        setProxyServiceState(STATE.OUTPUT_START);

    }

    private void outputStartEvent() throws FileNotFoundException {
        // Output Initialize //
        // You Could Change Address To Socket Linux File For Writing On Network //
        outPutChannelBuffer = outputIO.getOutputBuffer();
        Thread monitorThread = new Thread(outputIO);
        monitorThread.start();

        setProxyServiceState(STATE.TASK_START);
    }

    private void taskStartEvent() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, MalformedURLException, ClassNotFoundException, FileNotFoundException {
        // Create Pool And Run Tasks Per Record //
        for (int i = 0; i < sizeOfPool / 3; i++) {
            // Create New Applicator And Avoid Use Static On Tasks //
            processingTasksList.add(new ProcessingTask(this, i));
            executePool.execute(processingTasksList.get(i));
        }

        setProxyServiceState(STATE.INJECT_INIT_FILTER);
    }

    private void executeInitFilter() {
        synchronized (outPutChannelBuffer) {
            outPutChannelBuffer.put(ByteBuffer.wrap(filterApplicator.executeInitial(null).message.getBytes(StandardCharsets.UTF_8)));
        }

        setProxyServiceState(STATE.INPUT_START);
    }

    private void inputStartEvent() throws FileNotFoundException {
        // Ready For Start Input Controller //
        Thread inputThread = new Thread(inputIO);
        inputThread.start();

        setProxyServiceState(STATE.MONITOR);
    }

    private void monitorEvent() throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        // Monitor All Pipeline //
        // Algorithm For Slow Task //
        if (counterSlowPerformanceSeen > slowPerformanceSeenHighRisk) {
            counterSlowPerformanceSeen = 0;

            // Create New Applicator //
            processingTasksList.add(new ProcessingTask(this, processingTasksList.size()));
            executePool.execute(processingTasksList.get(processingTasksList.size() - 1));

            System.out.println("Due To The Huge Increase On Input Element Another Task Added To Pool : " + processingTasksList.get(processingTasksList.size() - 1).getTaskId());

        }

        if (primaryStorage.size() - lastSeenSize >= slowPerformanceTaskIndicator)
            counterSlowPerformanceSeen++;
        // Because of sleep on proxyService we take snapshot from size per .5 sec
        // So we take samples from size is  .5 sec So we have function per 2snapshot per sec
        lastSeenSize = primaryStorage.size();

        System.out.println("Storage Size: " + primaryStorage.size() + " Buffer Position: " + outPutChannelBuffer.position() + " Channel Size: " + outputIO.getOutputChannel().size());

    }

    private void inputEndEvent() {
        // Wait To Queue going to the empty and close all thread And shutdown //
        while (!primaryStorage.isEmpty()) ;

        setProxyServiceState(STATE.QUEUE_EMPTY);
    }

    private void queueEmptyEvent() {
        for (ProcessingTask task : processingTasksList)
            task.setShutdown(true);

        setProxyServiceState(STATE.TASK_DESTROYED);
    }

    private void taskDestroyEvent() {
        // Send Shutdown to remain thread in execute pool
        executePool.shutdownNow();

        setProxyServiceState(STATE.INJECT_END_FILTER);
    }

    private void executeEndFilter() {
        // At Last Run End Filter //
        synchronized (outPutChannelBuffer) {
            outPutChannelBuffer.put(ByteBuffer.wrap(filterApplicator.executeEnd(null).message.getBytes(StandardCharsets.UTF_8)));
        }

        setProxyServiceState(STATE.OUTPUT_END);
    }

    private void outputEndEvent() {
        // Shutdown Output Task //
        outputIO.setShutdown(true);

        setProxyServiceState(STATE.DESTROY);
    }

    private void destroyEvent() {
        // Free Resource //

        // Finish Event //
        setProxyServiceState(STATE.FINISH);
    }

    public STATE getProxyServiceState() {
        return proxyServiceState;
    }

    public void setProxyServiceState(STATE proxyServiceState) {
        this.proxyServiceState = proxyServiceState;
    }

    public ConcurrentLinkedQueue<ElementHolder> getPrimaryStorage() {
        return primaryStorage;
    }

    public OutputIO getOutputIO() {
        return outputIO;
    }

    public FilterApplicator<BaseFilterModel> createApplicator() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, MalformedURLException, ClassNotFoundException, FileNotFoundException {
        BaseDataModel dataModelInstance;
        BaseFilterModel filterModelInstance;

        if (!repositoryModel.isEmpty()) {
            URLClassLoader urlClassLoader = URLClassLoader.newInstance(new URL[]{new URL(repositoryModel)});
            dataModelInstance = (BaseDataModel) urlClassLoader.loadClass(dataModel).newInstance(); // Create With Default Constructor
            filterModelInstance = (BaseFilterModel) urlClassLoader.loadClass(filterModel).getConstructor(BaseDataModel.class).newInstance(dataModelInstance); // Create With Param
        } else {
            dataModelInstance = (BaseDataModel) Class.forName(dataModel).newInstance(); // Create With Default Constructor
            filterModelInstance = (BaseFilterModel) Class.forName(filterModel).getConstructor(BaseDataModel.class).newInstance(dataModelInstance); // Create With Param
        }

        // Anything About Ready Applicator Should Inject Here //
        // Check If Filter Need Sample Before Inject Into FilterApplicator //
        if (Boolean.valueOf(filterModelInstance.getClass().getAnnotation(BaseFilterModelAnnotation.class).IsNeededSample())) {
            filterModelInstance.takeSample(inputIO.initPointerToFile()); // Take Sample
            // Apply First Initial Filter To Sample Data //
            filterModelInstance.initialFilterList.get(0).apply(null);
        }

        // Create Filter Applier //
        return new FilterApplicator<>((BaseFilterModel) filterModelInstance);
    }

    @Override
    public void run() {
        while (getProxyServiceState() != STATE.FINISH) {
            try {
                Thread.sleep(sleepProxyServiceTime);
                System.out.println("State >>> " + proxyServiceState.toString());
                switch (this.proxyServiceState) {
                    case INIT:
                        init();
                        break;
                    case OUTPUT_START:
                        outputStartEvent();
                        break;
                    case TASK_START:
                        taskStartEvent();
                        break;
                    case INJECT_INIT_FILTER:
                        executeInitFilter();
                        break;
                    case INPUT_START:
                        inputStartEvent();
                        break;
                    case MONITOR:
                        monitorEvent();
                        break;
                    case INPUT_END:
                        inputEndEvent();
                        break;
                    case QUEUE_EMPTY:
                        queueEmptyEvent();
                        break;
                    case TASK_DESTROYED:
                        taskDestroyEvent();
                        break;
                    case INJECT_END_FILTER:
                        executeEndFilter();
                        break;
                    case OUTPUT_END:
                        outputEndEvent();
                        break;
                    case DESTROY:
                        destroyEvent();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public enum STATE {
        INIT,
        OUTPUT_START,
        TASK_START,
        INJECT_INIT_FILTER,
        INPUT_START,
        MONITOR,
        INPUT_END,
        QUEUE_EMPTY,
        INJECT_END_FILTER,
        TASK_DESTROYED,
        OUTPUT_END,
        DESTROY,
        FINISH
    }
}
