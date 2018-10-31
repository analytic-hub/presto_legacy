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
package com.facebook.presto.operator;

import com.google.common.util.concurrent.ListenableFuture;

import static com.google.common.util.concurrent.Futures.transform;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

public interface NestedLoopJoinPagesBridge
        extends JoinBridge
{
    ListenableFuture<NestedLoopJoinPages> getPagesFuture();

    ListenableFuture<?> setPages(NestedLoopJoinPages nestedLoopJoinPages);

    @Override
    void destroy();

    @Override
    default OuterPositionIterator getOuterPositionIterator()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    default ListenableFuture<?> whenBuildFinishes()
    {
        return transform(getPagesFuture(), ignored -> null, directExecutor());
    }
}
