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

import java.io.File;

import opennlp.tools.util.Sequence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author paco@sharethis.com
 */

public class
    Sentence
{
    // logging

    private final static Log LOG =
        LogFactory.getLog(Sentence.class.getName());


    /**
     * Public members.
     */

    public String text = null;
    public String[] token_list = null;
    public Node[] node_list = null;
    public String md5_hash = null;


    /**
     * Constructor.
     */

    public
	Sentence (final String text)
    {
	this.text = text;
    }


    /**
     * Return a byte array formatted as hexadecimal text.
     */

    public static String
	hexFormat (final byte[] b)
    {
	final StringBuilder sb = new StringBuilder(b.length * 2);

	for (int i = 0; i < b.length; i++) {
	    String h = Integer.toHexString(b[i]);

	    if (h.length() == 1) {
		sb.append("0");
	    }
	    else if (h.length() == 8) {
		h = h.substring(6);
	    }

	    sb.append(h);
	}

	return sb.toString().toUpperCase();
    }


    /**
     * Main processing per sentence.
     */

    public void
	mapTokens (final LanguageModel lang, final Graph graph)
	throws Exception
    {
	token_list = lang.tokenizeSentence(text);

	// scan each token to determine part-of-speech

	final String[] tag_list = lang.tagTokens(token_list);

	// create nodes for the graph

	Node last_node = null;
	node_list = new Node[token_list.length];

	for (int i = 0; i < token_list.length; i++) {
	    final String pos = tag_list[i];

	    if (LOG.isDebugEnabled()) {
		LOG.debug("token: " + token_list[i] + " pos tag: " + pos);
	    }

	    if (lang.isRelevant(pos)) {
		final String key = lang.getNodeKey(token_list[i], pos);
		final KeyWord value = new KeyWord(token_list[i], pos);
		final Node n = Node.buildNode(graph, key, value);

		// emit nodes to construct the graph

		if (last_node != null) {
		    n.connect(last_node);
		}

		last_node = n;
		node_list[i] = n;
	    }
	}
    }
}
