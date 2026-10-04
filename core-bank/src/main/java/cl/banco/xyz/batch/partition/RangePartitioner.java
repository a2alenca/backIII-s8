package cl.banco.xyz.batch.partition;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.LinkedHashMap;
import java.util.Map;

public class RangePartitioner implements Partitioner {

    private final int totalRecords;

    public RangePartitioner(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> partitions = new LinkedHashMap<>();
        int targetSize = (int) Math.ceil((double) totalRecords / gridSize);
        int start = 0;

        for (int i = 0; i < gridSize; i++) {
            ExecutionContext context = new ExecutionContext();
            int end = Math.min(start + targetSize - 1, totalRecords - 1);
            context.putInt("startItem", start);
            context.putInt("maxItems", end - start + 1);
            context.putString("partitionName", "partition_" + i);
            partitions.put("partition_" + i, context);
            start += targetSize;
            if (start >= totalRecords) {
                break;
            }
        }
        return partitions;
    }
}