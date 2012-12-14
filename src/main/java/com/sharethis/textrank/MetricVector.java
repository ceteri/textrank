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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.math.util.MathUtils;


/**
 * Implements a point in the vector space representing the distance
 * metric.
 *
 * @author paco@sharethis.com
 */

public class
    MetricVector
    implements Comparable<MetricVector>
{
    // logging

    private final static Log LOG =
        LogFactory.getLog(MetricVector.class.getName());


    /**
     * Public members.
     */

    public double metric = 0.0D;
    public NodeValue value = null;
    public double link_rank = 0.0D;
    public double count_rank = 0.0D;
    public double synset_rank = 0.0D;


    /**
     * Constructor.
     */

    public
	MetricVector (final NodeValue value, final double link_rank, final double count_rank, final double synset_rank)
    {
	this.value = value;

	this.metric = Math.sqrt(((1.0D * link_rank * link_rank) +
				 (0.5D * count_rank * count_rank) +
				 (1.5D * synset_rank * synset_rank)
				 ) / 3.0D
				);

	this.link_rank = MathUtils.round(link_rank, 2);
	this.count_rank = MathUtils.round(count_rank, 2);
	this.synset_rank = MathUtils.round(synset_rank, 2);

	if (LOG.isDebugEnabled()) {
	    LOG.debug("mv: " + metric + " " + link_rank + " " + count_rank + " " + synset_rank + " " + value.text);
	}
    }


    /**
     * Compare method for sort ordering.
     */

    public int
	compareTo (final MetricVector that)
    {
        if (this.metric > that.metric) {
	    return -1;
	}
	else if (this.metric < that.metric) {
	    return 1;
	}
	else {
	    return this.value.text.compareTo(that.value.text);
	}
    }


    /**
     * Serialize as text.
     */

    public String
	render ()
    {
	final StringBuilder sb = new StringBuilder();

	sb.append(MathUtils.round(metric, 1));
	sb.append(' ');
	sb.append(link_rank);
	sb.append(' ');
	sb.append(count_rank);
	sb.append(' ');
	sb.append(synset_rank);

	return sb.toString();
    }
}
