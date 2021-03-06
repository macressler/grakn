/*
 * Grakn - A Distributed Semantic Database
 * Copyright (C) 2016  Grakn Labs Limited
 *
 * Grakn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Grakn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Grakn. If not, see <http://www.gnu.org/licenses/gpl.txt>.
 */

package ai.grakn.graphs;

import ai.grakn.GraknGraph;
import ai.grakn.concept.ConceptId;
import ai.grakn.concept.EntityType;
import ai.grakn.concept.Instance;
import ai.grakn.concept.RelationType;
import ai.grakn.concept.RoleType;
import ai.grakn.concept.TypeLabel;
import ai.grakn.test.graphs.TestGraph;

import java.util.function.Consumer;

public class TransitivityMatrixGraph extends TestGraph {

    private final static TypeLabel key = TypeLabel.of("index");
    private final static String gqlFile = "simple-transitivity.gql";

    private final int n;
    private final int m;

    public TransitivityMatrixGraph(int n, int m){
        this.m = m;
        this.n = n;
    }

    public static Consumer<GraknGraph> get(int n, int m) {
        return new TransitivityMatrixGraph(n, m).build();
    }

    @Override
    public Consumer<GraknGraph> build(){
        return (GraknGraph graph) -> {
            loadFromFile(graph, gqlFile);
            buildExtensionalDB(graph, n, m);
        };
    }

    private void buildExtensionalDB(GraknGraph graph, int n, int m) {
        RoleType Qfrom = graph.getRoleType("Q-from");
        RoleType Qto = graph.getRoleType("Q-to");

        EntityType aEntity = graph.getEntityType("a-entity");
        RelationType Q = graph.getRelationType("Q");
        Instance aInst = putEntity(graph, "a", graph.getEntityType("entity2"), key);
        ConceptId[][] aInstanceIds = new ConceptId[n][m];
        for(int i = 0 ; i < n ;i++)
            for(int j = 0 ; j < m ;j++)
                aInstanceIds[i][j] = putEntity(graph, "a" + i + "," + j, aEntity, key).getId();
        
        Q.addRelation()
                .addRolePlayer(Qfrom, aInst)
                .addRolePlayer(Qto, graph.getConcept(aInstanceIds[0][0]));

        for(int i = 0 ; i < n ; i++) {
            for (int j = 0; j < m ; j++) {
                if ( i < n - 1 ) {
                    Q.addRelation()
                            .addRolePlayer(Qfrom, graph.getConcept(aInstanceIds[i][j]))
                            .addRolePlayer(Qto, graph.getConcept(aInstanceIds[i+1][j]));
                }
                if ( j < m - 1){
                    Q.addRelation()
                            .addRolePlayer(Qfrom, graph.getConcept(aInstanceIds[i][j]))
                            .addRolePlayer(Qto, graph.getConcept(aInstanceIds[i][j+1]));
                }
            }
        }
    }
}