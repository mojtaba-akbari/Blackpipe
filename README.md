BlackPipe

Idea:
Developed high performance proxy for injection filters on input data and write efficiency data on output
We should easily load any filters with any data model and apply on our input data

Design Pattern Used:
Multi Thread Event base pattern - Async Filtering Data 

Base Skeleton Concept:
Indeed BlackPipe is proxy service with high performance function that read data from input resource and 
Write data on output resource. but we could inject filter modules middle of pipeline, any filters cover data models
So for example our proxy could transfer our record from input to output:

input("Hello Developers") ----> Proxy-Service() ----> output("Hello Developers")

No we should inject filter on our record:

input("Hello Developers") ----> Proxy-Service(filter:<replace('Welcome Mojtaba To','Hello')>) ----> output("Wellcome Mojtaba To Developers")

Concept Schema:

![](/home/blackbase/Downloads/BlackPipe-Concept.png)


Design Schema:

![](/home/blackbase/Downloads/BlackPipe.png)


Ability:
1- Read At High Speed From Input File 
2- Control On Numbers Of Task By Simple Slow Performance Algorithm 
3- Load Filters And Data Models Dynamically (like kafka and spark...)
4- Control On Huge Buffer Allocate (like kafka and spark...)
5- Load DataModels And Filters Models With Annotation 
6- Write At High Speed On Output Resource (like kafka and spark...)
7- Control On Memory Over Flow Exception 
8- Concurrent Reading And Processing And Writing Output (like kafka and spark...)
9- Functional developing for creating filters (like kafka and spark...)
9- Under developing .... :) 

Functionality Test:
CSV Tested successfully
PRN Tested successfully

Performance Test:
I had tested with huge csv file size upper than 7G and more than 100 million records, with three scope of functionality (read,process,write)
Linux Command for generate huge file:

Remove first column of workbook2.csv from file then run below command on terminal
[while true;do cat workbook2.csv >> workload.csv; done]
After taking happy size, close terminal and add first column csv file to new file
Run service and result is [That took 239 seconds and generate 109078700 ID]
There is full log in file [last-test-result.log]
*** Beware of Huge File Please Add two args to the VM: -Xmx4G -Xms4G

Execute Service:
BlackPipe service has 8 args, you could take that with [help] arg. Example Of Args

""  <--- location for filters and model data , if it is empty service try load from classpath
"org.filters.CSVtoHTMLTableRenderFilter" <--- filter model
"org.models.CreditLimitDataModel" <--- data model
"/home/blackbase/workbook.csv" <--- input file provide full address
"/home/blackbase/workbook.html" <--- output file provide full address
2 <--- poolsize metric, service has fix pool size with (10 thread) , with this arg you could have fix*arg pool size 
2147483640 <--- 2g-2 buffer size , please provide this arg with byte , if you set 2g take error [exceed array size]
1932735283 <--- 1.9g water mark, this arg write buffer to output after this watermark

For first time execute service with upper arg to learn :)


At last:

Thanks!



BlackBase
