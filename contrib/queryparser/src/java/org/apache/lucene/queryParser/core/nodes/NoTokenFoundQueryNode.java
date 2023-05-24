package org.apache.lucene.queryParser.core.nodes;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.queryParser.core.parser.EscapeQuerySyntax;

/**
 * A {@link NoTokenFoundQueryNode} is used if a term is convert into no tokens
 * by the tokenizer/lemmatizer/analyzer (null).
 */
public class NoTokenFoundQueryNode extends DeletedQueryNode {

  private static final long serialVersionUID = 7332975497586993833L;

  public NoTokenFoundQueryNode() {
    super();
  }

  public CharSequence toQueryString(EscapeQuerySyntax escaper) {
    return "[NTF]";
  }

  public String toString() {
    return "<notokenfound/>";
  }

  public QueryNode cloneTree() throws CloneNotSupportedException {
    NoTokenFoundQueryNode clone = (NoTokenFoundQueryNode) super.cloneTree();

    // nothing to do here

    return clone;
  }
}
