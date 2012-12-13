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

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.dictionary.MorphologicalProcessor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;


/**
 * Access to WordNet through JWNL.
 * @author paco@sharethis.com
 * @author flo@leibert.de
 */

public class
    WordNet
{
    // logging

    private final static Log LOG =
        LogFactory.getLog(WordNet.class.getName());


    /**
     * Protected members.
     */

    protected static Dictionary dictionary = null;
    protected static MorphologicalProcessor mp = null;


    /**
     * Singleton
     */

    public static void
	buildDictionary (final String res_path, final String lang_code)
	throws Exception
    {
	// initialize the JWNL properties

	if (!JWNL.isInitialized()) {
	    final String model_path =
		res_path + "/" + lang_code;

	    final InputStream propertiesStream =
		buildPropertiesStream("wn_file_props.xml",
				      "/param[@name='file_manager']/param[@name='dictionary_path']",
				      model_path,
				      "wn"
				      );

	    JWNL.initialize(propertiesStream);

	    // build instances

	    dictionary = Dictionary.getInstance();
	    mp = dictionary.getMorphologicalProcessor();
	}
    }


    /**
     * Update the configuration so that we can create a properties
     * stream using it.
     */

    protected static InputStream
	buildPropertiesStream (final String cfg_file_name, final String query_string, final String resource_path, final String wn_dir_name)
	throws Exception
    {
	// load the config file as an XML document

	final File xml_file = new File(resource_path, cfg_file_name);
	final FileReader file_reader = new FileReader(xml_file);
	final BufferedReader buf_reader = new BufferedReader(file_reader);
	final SAXBuilder sax_builder = new SAXBuilder(false);
	final Document document = sax_builder.build(buf_reader);

	// use XPath to set the dictionary location (relative, not absolute)

	final StringBuilder query = new StringBuilder();
	query.append("jwnl_properties/dictionary");
	query.append(query_string);

	final XPath xpath = XPath.newInstance(query.toString());
	final Element path_node = (Element) xpath.selectSingleNode(document);
	final File wn_dir = new File(resource_path, wn_dir_name);
	path_node.setAttribute("value", wn_dir.getPath());

	// write the XML document as a stream

	final StringWriter writer = new StringWriter();
	final XMLOutputter xml_outputter = new XMLOutputter(Format.getPrettyFormat());

	xml_outputter.output(document.getRootElement(), writer);
	final String config_xml = writer.toString();

	return new ByteArrayInputStream(config_xml.getBytes());
    }


    /**
     * Access the Dictionary.
     */

    public static Dictionary
	getDictionary ()
    {
	return dictionary;
    }


    /**
     * Lookup the first lemma found.
     */

    public static IndexWord
	getLemma (final POS pos, final String derivation)
	throws JWNLException
    {
        return mp.lookupBaseForm(pos, derivation);
    }
}
