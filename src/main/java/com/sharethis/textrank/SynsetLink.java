/*
Copyright (c) 2009, ShareThis, Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.

    * Neither the name of the ShareThis, Inc., nor the names of its
      contributors may be used to endorse or promote products derived
      from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.sharethis.textrank;

import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implements a node value in a TextRank graph denoting a synset in
 * WordNet.
 *
 * @author paco@sharethis.com
 * @author flo@leibert.de
 */

public class
    SynsetLink
    extends NodeValue
{
    // logging

    private final static Log LOG =
        LogFactory.getLog(SynsetLink.class.getName());


    /**
     * Public members.
     */

    public static enum MyRelation { SYNONYM, HYPERNYM, SIBLING }

    public Synset synset = null;
    public Node parent = null;
    public int hops = 0;
    public MyRelation relation = null;


    /**
     * Constructor.
     */

    public
	SynsetLink (final String text, final Synset synset, final Node parent, final MyRelation relation, final int hops)
    {
	this.text = text;
	this.synset = synset;
	this.parent = parent;
	this.relation = relation;
	this.hops = hops;
    }


    /**
     * Create a description text for this value.
     */

    public String
	getDescription ()
    {
	final StringBuilder sb = new StringBuilder();

	sb.append(relation);
	sb.append('\t');
	sb.append(synset);

	return sb.toString();
    }


    /**
     * Foo
     */

    public static void
	addKeyWord (final Graph subgraph, final Node n, final String text, final POS pos)
	throws Exception
    {
	final IndexWord iw = WordNet.getLemma(pos, text);

	if (LOG.isDebugEnabled()) {
	    LOG.debug("n: " + n.key + " " + n.rank + " " + n.marked + " " + text);
	    LOG.debug(iw);
	}

	if (iw != null) {
	    for (Synset synset : iw.getSenses()) {
		if (LOG.isDebugEnabled()) {
		    LOG.debug("synset: " + synset);
		}

		final Node node_synset = testLink(subgraph, synset, n, MyRelation.SYNONYM, 1);

		if (node_synset != null) {
		    final Pointer[] hypernyms = synset.getPointers(PointerType.HYPERNYM);

		    for (Pointer hypernym : hypernyms) {
			final Synset hypernym_synset = hypernym.getTargetSynset();

			if (LOG.isDebugEnabled()) {
			    LOG.debug("hypernym: " + hypernym_synset);
			}

			final Node node_hypernym = testLink(subgraph, hypernym_synset, node_synset, MyRelation.HYPERNYM, 2);

			if (node_hypernym != null) {
			    final Pointer[] siblings = hypernym_synset.getPointers(PointerType.HYPONYM);

			    for (Pointer sibling : siblings) {
				final Synset sibling_synset = sibling.getTargetSynset();

				if (sibling_synset.getOffset() != synset.getOffset()) {
				    if (LOG.isDebugEnabled()) {
					LOG.debug("sibling: " + sibling_synset);
				    }

				    final Node node_sibling = testLink(subgraph, sibling_synset, node_hypernym, MyRelation.SIBLING, 3);
				}
			    }
			}
		    }
		}
	    }
	}
    }


    /**
     * Foo
     */

    public static Node
	testLink (final Graph synset_subgraph, final Synset synset, final Node parent, final MyRelation relation, final int hops)
	throws Exception
    {
	final String synset_key = Long.toString(synset.getOffset());

	Node node = synset_subgraph.get(synset_key);

	if (node == null) {
	    final SynsetLink synset_link = new SynsetLink(synset_key, synset, parent, relation, hops);

	    node = Node.buildNode(synset_subgraph, synset_key, synset_link);
	    node.connect(parent);

	    return node;
	}
	else {
	    final SynsetLink synset_link = (SynsetLink) node.value;

	    if (hops < synset_link.hops) {
		synset_link.relation = relation;
		synset_link.hops = hops;
	    }

	    node.connect(parent);

	    if (LOG.isDebugEnabled()) {
		LOG.debug("mark key on " + synset_key);
		LOG.debug("mark hit on " + node.value);
	    }

	    markAncestors(node);
	    markAncestors(parent);
	}

	return null;
    }


    /**
     * Traverse through this node's parent links, marking each to
     * avoid pruning.
     */

    public static void
	markAncestors (final Node node)
    {
	// recursively mark through synsets's parent links
	// recursively mark through found node's parent links, if not marked already

	if (!node.marked) {
	    if (LOG.isDebugEnabled()) {
		LOG.debug("marking: " + node.key);
	    }

	    node.marked = true;

	    if (node.value instanceof SynsetLink) {
		final SynsetLink synset_link = (SynsetLink) node.value;

		if (LOG.isDebugEnabled()) {
		    LOG.debug("recur marking: " + synset_link.synset);
		}

		markAncestors(synset_link.parent);
	    }
	}
    }


    /**
     * Prune the graph.
     */

    public static Graph
	pruneGraph (final Graph subgraph, final Graph graph)
    {
	final Graph new_subgraph = new Graph();

	for (Node n : subgraph.values()) {
	    if (n.marked) {
		graph.put(n.key, n);
		new_subgraph.put(n.key, n);
	    }
	    else {
		final SynsetLink s = (SynsetLink) n.value;
		n.disconnect(s.parent);
	    }
	}

	return new_subgraph;
    }


    /**
     * Determine a statistical distribution for the synset subgraph.
     */

    public static void
	calcStats (final Graph subgraph)
    {
	subgraph.dist_stats.clear();

	for (Node n : subgraph.values()) {
	    final SynsetLink synset_link = (SynsetLink) n.value;
	    double rank = n.rank;

	    switch (synset_link.relation) {
	    case HYPERNYM:
		rank = Math.sqrt(rank);

	    case SIBLING:
		rank = Math.sqrt(rank);

	    case SYNONYM:
	    default:
		break;
	    }

	    subgraph.dist_stats.addValue(rank);
	    n.rank = rank;
	}
    }
}
