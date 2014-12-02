/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.operator.window;

import com.facebook.presto.operator.PagesIndex;
import com.facebook.presto.spi.block.BlockBuilder;
import com.facebook.presto.spi.type.Type;

import java.util.List;

import static com.facebook.presto.spi.StandardErrorCode.INVALID_FUNCTION_ARGUMENT;
import static com.facebook.presto.spi.type.BigintType.BIGINT;
import static com.facebook.presto.util.Failures.checkCondition;

public class NTileFunction
        implements WindowFunction
{
    private final int valueChannel;

    private PagesIndex pagesIndex;
    private int currentPosition;
    private long rowCount;
    private long currentRow;

    public NTileFunction(List<Integer> argumentChannels)
    {
        this.valueChannel = argumentChannels.get(0);
    }

    @Override
    public Type getType()
    {
        return BIGINT;
    }

    @Override
    public void reset(int partitionStartPosition, int partitionRowCount, PagesIndex pagesIndex)
    {
        this.pagesIndex = pagesIndex;
        this.currentPosition = partitionStartPosition;
        this.rowCount = partitionRowCount;
        this.currentRow = 0;
    }

    @Override
    public void processRow(BlockBuilder output, boolean newPeerGroup, int peerGroupCount)
    {
        if (pagesIndex.isNull(valueChannel, currentPosition)) {
            output.appendNull();
        }
        else {
            long buckets = pagesIndex.getLong(valueChannel, currentPosition);
            checkCondition(buckets > 0, INVALID_FUNCTION_ARGUMENT, "Buckets must be greater than 0");
            BIGINT.writeLong(output, bucket(buckets) + 1);
        }

        currentPosition++;
        currentRow++;
    }

    private long bucket(long buckets)
    {
        if (rowCount < buckets) {
            return currentRow;
        }

        long remainderRows = rowCount % buckets;
        long rowsPerBucket = rowCount / buckets;

        // Remainder rows are assigned starting from the first bucket.
        // Thus, each of those buckets have an additional row.
        if (currentRow < ((rowsPerBucket + 1) * remainderRows)) {
            return currentRow / (rowsPerBucket + 1);
        }

        // Shift the remaining rows to account for the remainder rows.
        return (currentRow - remainderRows) / rowsPerBucket;
    }
}
