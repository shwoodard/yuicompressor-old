/*
 * YUI Compressor
 * Author: Julien Lecomte <jlecomte@yahoo-inc.com>
 * Copyright (c) 2007, Yahoo! Inc. All rights reserved.
 * Code licensed under the BSD License:
 *     http://developer.yahoo.net/yui/license.txt
 */

package com.yahoo.platform.yui.compressor;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.UnknownOptionException;

import java.util.HashMap;
import java.util.Map;

public class YUICompressor {

    public static void main(String args[]) {

        CmdLineParser parser = new CmdLineParser();
        Map<String, CmdLineParser.Option> options = new HashMap<String, CmdLineParser.Option>();
        options.put("typeOpt", parser.addStringOption("type"));
        options.put("verboseOpt", parser.addBooleanOption('v', "verbose"));
        options.put("nomungeOpt", parser.addBooleanOption("nomunge"));
        options.put("linebreakOpt", parser.addStringOption("line-break"));
        options.put("preserveSemiOpt", parser.addBooleanOption("preserve-semi"));
        options.put("disableOptimizationsOpt", parser.addBooleanOption("disable-optimizations"));
        options.put("helpOpt", parser.addBooleanOption('h', "help"));
        options.put("charsetOpt", parser.addStringOption("charset"));
        


        try {
			parser.parse(args);
		} catch (IllegalOptionValueException e) {
			usage();
			System.exit(0);
		} catch (UnknownOptionException e) {
			//TODO how handle
			usage();
			System.exit(0);
		}

        Boolean help = (Boolean) parser.getOptionValue(options.get("helpOpt"));
        if (help != null && help.booleanValue()) {
            usage();
            System.exit(0);
        }

        (new Application()).run(parser, options);
    }

    public static void usage() {
        System.out.println(
                "\nUsage: java -jar yuicompressor-x.y.z.jar [options] [input file]\n\n"

                        + "Global Options\n"
                        + "  -h, --help                Displays this information\n"
                        + "  --type <js|css>           Specifies the type of the input file\n"
                        + "  --charset <charset>       Read the input file using <charset>\n"
                        + "  --line-break <column>     Insert a line break after the specified column number\n"
                        + "  -v, --verbose             Display informational messages and warnings\n"
                        + "  -o <file>                 Place the output into <file>. Defaults to stdout.\n\n"

                        + "JavaScript Options\n"
                        + "  --nomunge                 Minify only, do not obfuscate\n"
                        + "  --preserve-semi           Preserve all semicolons\n"
                        + "  --disable-optimizations   Disable all micro optimizations\n\n"

                        + "If no input file is specified, it defaults to stdin. In this case, the 'type'\n"
                        + "option is required. Otherwise, the 'type' option is required only if the input\n"
                        + "file extension is neither 'js' nor 'css'.");
    }
}
