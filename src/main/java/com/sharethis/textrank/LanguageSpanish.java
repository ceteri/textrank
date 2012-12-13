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

import opennlp.tools.lang.spanish.PosTagger;
import opennlp.tools.lang.spanish.SentenceDetector;
import opennlp.tools.lang.spanish.Tokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.tartarus.snowball.ext.SpanishStemmer;


/**
 * Implementation of Spanish-specific tools for natural language
 * processing.
 *
 * @author paco@sharethis.com
 */

public class
    LanguageSpanish
    extends LanguageModel
{
    // logging

    private final static Log LOG =
        LogFactory.getLog(LanguageSpanish.class.getName());


    /**
     * Public definitions.
     */

    public static SentenceDetector splitter_es = null;
    public static Tokenizer tokenizer_es = null;
    public static PosTagger tagger_es = null;
    public static SpanishStemmer stemmer_es = null;


    /**
     * Constructor. Not quite a Singleton pattern but close enough
     * given the resources required to be loaded ONCE.
     */

    public
	LanguageSpanish (final String path)
	throws Exception
    {
	if (splitter_es == null) {
	    loadResources(path);
	}
    }


    /**
     * Load libraries for OpenNLP for this specific language.
     */

    public void
	loadResources (final String path)
	throws Exception
    {
	splitter_es =
	    new SentenceDetector((new File(path, "opennlp/SpanishSent.bin.gz")).getPath());

	tokenizer_es =
	    new Tokenizer((new File(path, "opennlp/SpanishTok.bin.gz")).getPath());

	tagger_es =
	    new PosTagger((new File(path, "opennlp/SpanishPOS.bin.gz")).getPath());

	stemmer_es =
	    new SpanishStemmer();
    }


    /**
     * Split sentences within the paragraph text.
     */

    public String[]
	splitParagraph (final String text)
    {
	return splitter_es.sentDetect(text);
    }


    /**
     * Tokenize the sentence text into an array of tokens.
     */

    public String[]
	tokenizeSentence (final String text)
    {
	return tokenizer_es.tokenize(text);
    }


    /**
     * Run a part-of-speech tagger on the sentence token list.
     */

    public String[]
	tagTokens (final String[] token_list)
    {
	return tagger_es.tag(token_list);
    }


    /**
     * Prepare a stable key for a graph node (stemmed, lemmatized)
     * from a token.
     */

    public String
	getNodeKey (final String text, final String pos)
        throws Exception
    {
	return pos.substring(0, 2) + stemToken(scrubToken(text)).toLowerCase();
    }


    /**
     * Determine whether the given PoS tag is a noun.
     */

    public boolean
	isNoun (final String pos)
    {
	return pos.startsWith("NC");
    }


    /**
     * Determine whether the given PoS tag is an adjective.
     */

    public boolean
	isAdjective (final String pos)
    {
	return pos.startsWith("AQ");
    }


    /**
     * Perform stemming on the given token.
     */

    public String
	stemToken (final String token)
    {
	stemmer_es.setCurrent(token);
	stemmer_es.stem();

	return stemmer_es.getCurrent();
    }
}
