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
package com.facebook.presto.sql.planner.assertions;

import com.facebook.presto.Session;
import com.facebook.presto.cost.PlanNodeCost;
import com.facebook.presto.metadata.Metadata;
import com.facebook.presto.sql.planner.plan.ApplyNode;
import com.facebook.presto.sql.planner.plan.PlanNode;

import java.util.List;

import static com.facebook.presto.sql.planner.assertions.MatchResult.NO_MATCH;
import static com.facebook.presto.sql.planner.assertions.MatchResult.match;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

public class CorrelationMatcher
        implements Matcher
{
    private final List<String> correlation;

    CorrelationMatcher(List<String> correlation)
    {
        this.correlation = requireNonNull(correlation, "correlation is null");
    }

    @Override
    public boolean shapeMatches(PlanNode node)
    {
        return node instanceof ApplyNode;
    }

    @Override
    public MatchResult detailMatches(PlanNode node, PlanNodeCost cost, Session session, Metadata metadata, SymbolAliases symbolAliases)
    {
        checkState(shapeMatches(node), "Plan testing framework error: shapeMatches returned false in detailMatches in %s", this.getClass().getName());

        ApplyNode applyNode = (ApplyNode) node;

        if (correlation.size() != applyNode.getCorrelation().size()) {
            return NO_MATCH;
        }

        int i = 0;
        for (String alias : correlation) {
            if (!symbolAliases.get(alias).equals(applyNode.getCorrelation().get(i++).toSymbolReference())) {
                return NO_MATCH;
            }
        }
        return match();
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("correlation", correlation)
                .toString();
    }
}
