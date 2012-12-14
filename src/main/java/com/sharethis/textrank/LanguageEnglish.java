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

import opennlp.tools.lang.english.ParserTagger;
import opennlp.tools.lang.english.SentenceDetector;
import opennlp.tools.lang.english.Tokenizer;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.util.Sequence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.tartarus.snowball.ext.EnglishStemmer;

import spiaotools.SentParDetector;


/**
 * Implementation of English-specific tools for natural language
 * processing.
 *
 * @author paco@sharethis.com
 */

public class
    LanguageEnglish
    extends LanguageModel
{
    // logging

    private final static Log LOG =
        LogFactory.getLog(LanguageEnglish.class.getName());


    /**
     * Public definitions.
     */

    public static SentParDetector splitter_en = null;
    /** /
    public static SentenceDetectorME splitter_en = null;
    /* */
    public static Tokenizer tokenizer_en = null;
    public static ParserTagger tagger_en = null;
    public static EnglishStemmer stemmer_en = null;


    /**
     * Constructor. Not quite a Singleton pattern but close enough
     * given the resources required to be loaded ONCE.
     */

    public
	LanguageEnglish (final String path)
	throws Exception
    {
	if (splitter_en == null) {
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
	splitter_en = new SentParDetector();

	/** /
	splitter_en =
		new SentenceDetector((new File(path, "opennlp/EnglishSD.bin.gz")).getPath());
	/* */

	tokenizer_en =
	    new Tokenizer((new File(path, "opennlp/EnglishTok.bin.gz")).getPath());

	tagger_en =
	    new ParserTagger((new File(path, "opennlp/tag.bin.gz")).getPath(),
			     (new File(path, "opennlp/tagdict")).getPath(),
			     false
			     );

	stemmer_en =
	    new EnglishStemmer();
    }


    /**
     * Split sentences within the paragraph text.
     */

    public String[]
	splitParagraph (final String text)
    {
	return splitter_en.markupRawText(2, text).split("\\n");

	/** /
	return splitter_en.sentDetect(text);
	/* */
    }


    /**
     * Tokenize the sentence text into an array of tokens.
     */

    public String[]
	tokenizeSentence (final String text)
    {
	final String[] token_list = tokenizer_en.tokenize(text);

	for (int i = 0; i < token_list.length; i++) {
	    token_list[i] = token_list[i].replace("\"", "").toLowerCase().trim();
	}

	return token_list;
    }


    /**
     * Run a part-of-speech tagger on the sentence token list.
     */

    public String[]
	tagTokens (final String[] token_list)
    {
	final Sequence[] sequences = tagger_en.topKSequences(token_list);
	final String[] tag_list = new String[token_list.length];

	int i = 0;

	for (Object obj : sequences[0].getOutcomes()) {
	    tag_list[i] = (String) obj;
	    i++;
	}

	return tag_list;
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
	return pos.startsWith("NN");
    }


    /**
     * Determine whether the given PoS tag is an adjective.
     */

    public boolean
	isAdjective (final String pos)
    {
	return pos.startsWith("JJ");
    }


    /**
     * Perform stemming on the given token.
     */

    public String
	stemToken (final String token)
    {
	stemmer_en.setCurrent(token);
	stemmer_en.stem();

	return stemmer_en.getCurrent();
    }
}
