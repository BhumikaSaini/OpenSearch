/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearch.action.admin.cluster.shards.routing.weighted.get;

import org.opensearch.OpenSearchParseException;
import org.opensearch.action.ActionResponse;

import org.opensearch.cluster.routing.WeightedRouting;
import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;
import org.opensearch.common.xcontent.ToXContentObject;
import org.opensearch.common.xcontent.XContentBuilder;
import org.opensearch.common.xcontent.XContentParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.opensearch.common.xcontent.XContentParserUtils.ensureExpectedToken;

/**
 * Response from fetching weights for weighted round-robin search routing policy.
 *
 * @opensearch.internal
 */
public class ClusterGetWeightedRoutingResponse extends ActionResponse implements ToXContentObject {
    public WeightedRouting getWeightedRouting() {
        return weightedRouting;
    }

    private final WeightedRouting weightedRouting;

    public Boolean getDiscoveredClusterManager() {
        return discoveredClusterManager;
    }

    private final Boolean discoveredClusterManager;

    private static final String DISCOVERED_CLUSTER_MANAGER = "discovered_cluster_manager";

    ClusterGetWeightedRoutingResponse() {
        this.weightedRouting = null;
        this.discoveredClusterManager = null;
    }

    public ClusterGetWeightedRoutingResponse(WeightedRouting weightedRouting, Boolean discoveredClusterManager) {
        this.discoveredClusterManager = discoveredClusterManager;
        this.weightedRouting = weightedRouting;
    }

    ClusterGetWeightedRoutingResponse(StreamInput in) throws IOException {
        if (in.available() != 0) {
            this.weightedRouting = new WeightedRouting(in);
            this.discoveredClusterManager = in.readOptionalBoolean();
        } else {
            this.weightedRouting = null;
            this.discoveredClusterManager = null;
        }
    }

    /**
     * List of weights to return
     *
     * @return list or weights
     */
    public WeightedRouting weights() {
        return this.weightedRouting;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        if (weightedRouting != null) {
            weightedRouting.writeTo(out);
        }
        if (discoveredClusterManager != null) {
            out.writeOptionalBoolean(discoveredClusterManager);
        }
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        if (this.weightedRouting != null) {
            for (Map.Entry<String, Double> entry : weightedRouting.weights().entrySet()) {
                builder.field(entry.getKey(), entry.getValue().toString());
            }
            if (discoveredClusterManager != null) {
                builder.field(DISCOVERED_CLUSTER_MANAGER, discoveredClusterManager);
            }
        }
        builder.endObject();
        return builder;
    }

    public static ClusterGetWeightedRoutingResponse fromXContent(XContentParser parser) throws IOException {
        ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.nextToken(), parser);
        XContentParser.Token token;
        String attrKey = null, attrValue = null;
        Boolean discoveredClusterManager = null;
        Map<String, Double> weights = new HashMap<>();

        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                attrKey = parser.currentName();
            } else if (token == XContentParser.Token.VALUE_STRING) {
                attrValue = parser.text();
                if (attrKey != null) {
                    weights.put(attrKey, Double.parseDouble(attrValue));
                }
            } else if (token == XContentParser.Token.VALUE_BOOLEAN && attrKey != null && attrKey.equals(DISCOVERED_CLUSTER_MANAGER)) {
                discoveredClusterManager = Boolean.parseBoolean(parser.text());
            } else {
                throw new OpenSearchParseException("failed to parse weighted routing response");
            }
        }
        WeightedRouting weightedRouting = new WeightedRouting("", weights);
        return new ClusterGetWeightedRoutingResponse(weightedRouting, discoveredClusterManager);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClusterGetWeightedRoutingResponse that = (ClusterGetWeightedRoutingResponse) o;
        return weightedRouting.equals(that.weightedRouting) && discoveredClusterManager.equals(that.discoveredClusterManager);
    }

    @Override
    public int hashCode() {
        return Objects.hash(weightedRouting, discoveredClusterManager);
    }
}
