# Overview

In this repo, we use Spark Streaming to pull the flow information from PCAP files that are being written live to MapR FS.

Captures happen using tcpdump which writes pcap files via NFS to land the raw data in MapR FS.

Eventually, this will hopefully be able to deal with very high ingest rate, multiple 40Gbps interfaces being captured.

# Building

You need sbt (http://www.scala-sbt.org/).

    sbt package

# Running

## Configure the scripts

In the cloned directory is a script called `env.sh`. Edit it to your liking, changing the paths as needed.

`FILE_LIMIT` is used to limit the number of files tcpdump will create in a single run using the `-W` option.

`TIME_LIMIT` is passed to tcpdump as an argument to the -G option, which limits the amount of time a given capture file covers.

Once `FILE_LIMIT` is reached, tcpdump will exit.

`env.sh` tries to help you out by selecting an interface to use with tcpdump. It will find the interface with the default route for the node, and use that one. You can comment this out and hard-code another interface if you like.

## Start the Spark Streaming Job

You can do this first.

Use the `submit.sh` script like so.

    ./submit.sh /user/vgonzalez/pcap/in/`date +%Y/%m/%d` /user/vgonzalez/pcap/out

The script defaults to using a YARN cluster. If you want to run locally, edit the script and change `SPARK_MASTER` to something like `local[2]`. You can also use a spark-standalone cluster by supplying the spark master URL.

### Input

The first path in the submit.sh invocation is the input directory, where Spark will look for new files. The second is the output directory, where the job will store output.

Notice that the input directory is at the end of a pathname that has the pcap files organized by day - this makes it very easy to create a MapR volume for each day. The date components need to be created before this is run - the `run_tcpdump.sh` script discussed below will take care of this for you.

### Output

Parquet files will appear under the output directory in a subdirectory called `flows`. Other data can appear in other subdirectories later as functionality gets added. The data in the `flows` directory will be partitioned by date as well. The date partitions are there to help with query in Drill. They also help us to locate the files associated with a particular timeframe.

## Start the monitor_and_move script

Using tcpdump to write pcap files to MapR FS via the NFS gateway is convenient but since PCAP files are not splittable and tcpdump does not have a mode of operation that renames files when complete, we need another way to make sure that files are only considered by spark streaming when they are no longer being written to.  

We manage this with the `monitor_and_move.sh` script, which will monitor the input directory looking for new files. When it detects a new file has been created the script will list the directory and find the second-newest one. The newest will still be getting written to, and the second newest will be the file we want to move. The script will then rename this file, removing the leading `.`, which will permit spark streaming to consider the file in the next batch.

Anyway, run the script:

    ./monitor_and_move

If you have not yet started tcpdump via the script below, `monitor_and_move` will pace back and forth impatiently until the input directory is created.

## Start tcpdump

Having configured `env.sh` above, you can use the supplied tcpdump script to run tcpdump. Edit the script and examine the options, noting that the interface selection in `env.sh` may need some tweaks.

    ./run_tcpdump.sh


# Now what?

## YARN & Spark Application GUI

On your resourcemanager GUI, find the running application. You should see an application with the name `com.mapr.pcapstream.PcapStream`. Once you find that line, locate the "Tracking UI" column (you may need to scroll right). Click the ApplicationMaster link.

Now you should be looking at the Spark application UI. Have a look around.

Under the Jobs tab, click on the Event Timeline link. Check out the timeline view of the streaming job. If tcpdump has seen any data, you should see some little blue boxes appearing at somewhat regular intervals. Click on one of the small blue boxes and have a look at the job the box represents. 

## Drill

Since the streaming job is kicking out Parquet files, why not make the data available to BI tools via Drill? Fire up sqlline and try something like this on the output directory:

```sql
select count(1) from dfs.`/user/vgonzalez/pcap/out/flows`;
```

Run it a few times, and see that the count changes. That's of course because we're creating new Parquet files with each streaming batch.

Some aggregations by protocol:

```sql
select 
    protocol,
    count(*) as packets,
    max(length) as maxLength,
    avg(length) as avgLength 
  from dfs.`/user/vgonzalez/pcap/out/flows`
  group by protocol;
```

And a more complex query to see the packet count per millisecond:

```sql
select 
    cast(to_timestamp(cast(timestampMillis/1000 as bigint)) as date) as tsDate,
    date_part('hour', to_timestamp(cast(timestampMillis/1000 as bigint))) as tsHour,
    date_part('minute', to_timestamp(cast(timestampMillis/1000 as bigint))) as tsMinute,
    date_part('second', to_timestamp(cast(timestampMillis/1000 as bigint))) as tsSecond,
    timestampMillis,
    srcPort,
    dstPort,
    length,
    count(*) over(partition by timestampMillis) as countPerMilli,
    avg(length) over(partition by timestampMillis) as avgLengthPerMilli 
  from dfs.`/user/vgonzalez/pcap/out/flows`;
```

# TODO

* Push flow data into Elasticsearch or Solr with each batch

* Push flow data onto a Kafka topic for pull by ES?

* ~~Output the flow data as parquet for query by BI tools~~

* Include a directory path for the files

* Good naming convention for the pcap filenames so we can pull them back

* Dashboard the flow data in Kibana

* Should `monitor_and_move.sh` do something with older `.dump*` files?

