import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.lib.db.DBWritable;

public class DBOutputWritable implements Writable, DBWritable {
    private String starting_phrase;
    private String following_word;
    private int count;
    public DBOutputWritable(String starting_phrase, String word, int count) {
        this.starting_phrase = starting_phrase;
        this.following_word = word;
        this.count = count;
    }

    public void write(DataOutput dataOutput) throws IOException {

    }

    public void readFields(DataInput dataInput) throws IOException {

    }

    public void write(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, starting_phrase);
        preparedStatement.setString(2, following_word);
        preparedStatement.setInt(3, count);

    }

    public void readFields(ResultSet resultSet) throws SQLException {
        starting_phrase = resultSet.getString(1);
        following_word = resultSet.getString(2);
        count = resultSet.getInt(3);

    }
}
