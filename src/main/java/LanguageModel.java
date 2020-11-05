import java.util.*;
import java.io.IOException;

import com.sun.org.apache.bcel.internal.classfile.ConstantObject;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;


public class LanguageModel {
    // second mapreduce job, use the probabilty to predict the next word based on starting phrase,
    // store top 10 word into the database
    public static class Map extends Mapper<LongWritable, Text, Text, Text> {
        // input NGram library, I love big data \t10 , HDFS split key value pair using \t
        // output: key = I love big, value = data=10

        int threashold;
        @Override
        // get the threashold parameter from the configuration
        public void setup(Context context) {
            Configuration conf = context.getConfiguration();
            threashold = conf.getInt("threashold", 5);
        }
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException{
            String line = value.toString().trim();

            // split phrase and count
            String[] wordsPlusCount = line.split("\t");
            String[] words = wordsPlusCount[0].split("\\s+");
            int count = Integer.valueOf(wordsPlusCount[wordsPlusCount.length - 1]);

            // if line is null or empty, or incomplete, or count less than threashold
            if ((wordsPlusCount.length < 2) || (count <= threashold)) {
                return;
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < words.length - 1; i++) {
                sb.append(words[i]);
                sb.append(" ");
            }

            String startingPhrase = sb.toString().trim();
            String nextWord = words[words.length - 1] + "=" + count;
            if (startingPhrase.length() > 0) {
                context.write(new Text(startingPhrase), new Text(nextWord));
            }

        }
    }

    public static class Reduce extends Reducer<Text, Text, DBOutputWritable, NullWritable> {
        // only write top n to the database

        int n;


        // get the n parameter from the configuration
        @Override
        public void setup(Context context) {
            Configuration conf = context.getConfiguration();
            n = conf.getInt("n", 5);
        }

        @Override
        public void reduce(Text key,Iterable<Text> values, Context context) throws IOException, InterruptedException {
            // <is=1000, is book=10>
            TreeMap<Integer, List<String>> treeMap = new TreeMap<Integer, List<String>>(Collections.<Integer>reverseOrder());
            for (Text val : values) {
                String cur = val.toString().trim();
                String word = cur.split("=")[0].trim();
                int count = Integer.parseInt( cur.split("=")[1].trim());
                if (treeMap.containsKey(count)) {
                    treeMap.get(count).add(word);
                } else {
                    List<String> list = new ArrayList<String>();
                    list.add(word);
                    treeMap.put(count, list);
                }
            }

            Iterator<Integer> iter = treeMap.keySet().iterator();

            for(int j=0 ; iter.hasNext() && j < n;) {
                int keyCount = iter.next();
                List<String> words = treeMap.get(keyCount);
                for(int i = 0; i < words.size() && j < n; i++) {
                    context.write(new DBOutputWritable(key.toString(), words.get(i), keyCount), NullWritable.get());
                    j++;
                }
            }
        }

    }
}