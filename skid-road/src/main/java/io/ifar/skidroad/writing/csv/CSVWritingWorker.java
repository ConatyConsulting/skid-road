package io.ifar.skidroad.writing.csv;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;

import com.fasterxml.jackson.dataformat.csv.CsvFactory;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.ifar.goodies.Tuple;
import io.ifar.skidroad.LogFile;
import io.ifar.skidroad.tracking.LogFileTracker;
import io.ifar.skidroad.writing.AbstractWritingWorker;

import static java.nio.file.StandardOpenOption.*;

/**
 * CSV-based {@link io.ifar.skidroad.writing.AbstractWritingWorker} implementation that consumes Tuples.
 */
public class CSVWritingWorker<T extends Tuple> extends AbstractWritingWorker<CsvGenerator, T> {
    /**
     * We don't have 'real' schema to bind columns to named values; but we still need to
     * specify details of underlying CSV format
     */
    private final static CsvSchema CSV_SCHEMA =
            CsvSchema.emptySchema()
                    .withNullValue("\\N")
                    .withoutHeader()
                    .withoutEscapeChar()
                    .withQuoteChar('"')
                    .withColumnSeparator(',')
                    .withLineSeparator("\r\n");

    private final static CsvFactory csvFactory = new CsvFactory();
    private final CsvSchema schema;

    public CSVWritingWorker(final BlockingQueue<T> queue, final LogFile logFileRecord, final int maxFlushIntervalSeconds,
                            final String nullRepresentation, final LogFileTracker tracker)
    {
        super(queue, logFileRecord, maxFlushIntervalSeconds, tracker);
        if (nullRepresentation != null) {
            schema = CSV_SCHEMA.rebuild().setNullValue(nullRepresentation).build();
        } else {
            schema = CSV_SCHEMA;
        }
    }

    @Override
    protected CsvGenerator openForWriting(Path path) throws IOException {
        OutputStream out = Files.newOutputStream(path, CREATE, WRITE, APPEND);
        CsvGenerator generator = csvFactory.createGenerator(out);
        generator.setSchema(schema);
        return generator;
    }

    @Override
    protected void writeItem(CsvGenerator gen, Tuple item) throws IOException {
        gen.writeStartArray();
        for (Object value : item.getValues()) {
            if (value == null) {
                gen.writeString("\\N"); //Can't writeNull because Jackson skips nulls in arrays
            } else if (value instanceof Boolean) {
                gen.writeBoolean(((Boolean) value).booleanValue());
            } else { // no point in distinguishing numbers etc; all end up as Strings anyway
                gen.writeString(value.toString());
            }
        }
        gen.writeEndArray();
    }
}
