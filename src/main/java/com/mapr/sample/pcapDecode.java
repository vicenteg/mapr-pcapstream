package com.mapr.sample;



import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import edu.gatech.sjpcap.*;
import org.json.simple.JSONObject;

public class pcapDecode {

    public static class PacketDecodeMapper extends Mapper<NullWritable, BytesWritable, Text, Text> {


        public void map(NullWritable key, BytesWritable value, Context context
                       ) throws IOException, InterruptedException {
            /*
                  StringTokenizer itr = new StringTokenizer(value.toString());
                  while (itr.hasMoreTokens()) {
                    word.set(itr.nextToken());
                    context.write(word, one);
                  }
            */
            String fileName =  ((FileSplit) context.getInputSplit()).getPath().getName();
            PcapParser pcapParser = new PcapParser();
            pcapParser.openFile(value.getBytes());
            Packet packet = pcapParser.getPacket();
            while (packet != Packet.EOF) {
                if (!(packet instanceof IPPacket)) {
                    packet = pcapParser.getPacket();
                    continue;
                }
                JSONObject obj=new JSONObject();
                IPPacket ipPacket = (IPPacket) packet;
                if (ipPacket instanceof UDPPacket) {
                    UDPPacket udpPacket = (UDPPacket) ipPacket;
                    obj.put("@timestamp",(ipPacket.timestamp / 1000));
                    obj.put("src_ip",ipPacket.src_ip.getHostAddress());
                    obj.put("dst_ip",ipPacket.dst_ip.getHostAddress());
                    obj.put("src_port",udpPacket.src_port);
                    obj.put("dst_port",udpPacket.dst_port);
                    obj.put("proto","UDP");
                    obj.put("length",udpPacket.data.length);
                    obj.put("filename",fileName);
                }
                if (ipPacket instanceof TCPPacket) {
                    TCPPacket tcpPacket = (TCPPacket) ipPacket;
                    obj.put("@timestamp",(ipPacket.timestamp / 1000));
                    obj.put("src_ip",ipPacket.src_ip.getHostAddress());
                    obj.put("dst_ip",ipPacket.dst_ip.getHostAddress());
                    obj.put("src_port",tcpPacket.src_port);
                    obj.put("dst_port",tcpPacket.dst_port);
                    obj.put("proto","TCP");
                    obj.put("length",tcpPacket.data.length);
                    obj.put("filename",fileName);
                }
                context.write(new Text(fileName), new Text(obj.toString()));
                packet = pcapParser.getPacket();
            }

        }
    }

    public static class PacketOutputReducer
        extends Reducer<Text,IntWritable,Text,IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
                          ) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

    public static void mapReduceMain(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if (otherArgs.length != 2) {
            System.err.println("Usage: pcapDecode <in> <out>");
            System.exit(2);
        }
        Job job = new Job(conf, "PCAP Decoder");
        job.setJarByClass(pcapDecode.class);

        job.setInputFormatClass(WholeFileInputFormat.class);
        job.setMapperClass(PacketDecodeMapper.class);

//      job.setCombinerClass(PacketOutputReducer.class);
//      job.setReducerClass(PacketOutputReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        WholeFileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
