package com.yahoo.platform.yui.compressor;

import jargs.gnu.CmdLineParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

/**
 * @author sam
 * 
 */
public class Application {

	private static Writer out;
	private static Reader in;

	/**
	 * 
	 */
	public void run(CmdLineParser parser,
			Map<String, CmdLineParser.Option> options) {
		try {

			boolean verbose = parser.getOptionValue(options.get("verboseOpt")) != null;

			String charset = (String) parser.getOptionValue(options
					.get("charsetOpt"));
			if (charset == null || !Charset.isSupported(charset)) {
				charset = System.getProperty("file.encoding");
				if (charset == null) {
					charset = "UTF-8";
				}
				if (verbose) {
					System.err.println("\n[INFO] Using charset " + charset);
				}
			}

			int linebreakpos = -1;
			String linebreakstr = (String) parser.getOptionValue(options
					.get("linebreakOpt"));
			if (linebreakstr != null) {
				try {
					linebreakpos = Integer.parseInt(linebreakstr, 10);
				} catch (NumberFormatException e) {
					YUICompressor.usage();
					System.exit(1);
				}
			}

			boolean munge = parser.getOptionValue(options.get("nomungeOpt")) == null;
			boolean preserveAllSemiColons = parser.getOptionValue(options
					.get("preserveSemiOpt")) != null;
			boolean disableOptimizations = parser.getOptionValue(options
					.get("disableOptimizationsOpt")) != null;

			String[] fileArgs = parser.getRemainingArgs();

			if (fileArgs.length == 0) {
				YUICompressor.usage();
				System.exit(1);
			}

			for (String filePath : fileArgs) {
				File currFile = new File(filePath);
				String[] fileNamePieces = currFile.getName().split("\\.");

				if (fileNamePieces.length < 2) {
					YUICompressor.usage();
					System.exit(1);
				}

				String type = fileNamePieces[fileNamePieces.length - 1];

				if (type != null && !type.equalsIgnoreCase("js")
						&& !type.equalsIgnoreCase("css")) {
					YUICompressor.usage();
					System.exit(1);
				}

				addFileToBuffer(currFile, type, parser, options, charset,
						linebreakpos, verbose, munge, preserveAllSemiColons,
						disableOptimizations);
			}
		} catch (IOException e) {

			e.printStackTrace();
			System.exit(1);

		} finally {
			// just in case
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void addFileToBuffer(File file, String type, CmdLineParser parser,
			Map<String, CmdLineParser.Option> options, String charSet,
			int lineBreakPos, boolean verbose, boolean munge,
			boolean preserveAllSemiColons, boolean disableOptimizations)
			throws IOException {
		if (type.equalsIgnoreCase("js")) {
				compressJsFile(file, charSet, lineBreakPos, verbose, munge,
						preserveAllSemiColons, disableOptimizations);
		} else if (type.equalsIgnoreCase("css")) {
			compressCssFile(file, charSet, lineBreakPos);
		}
	}

	private void compressCssFile(File file, String charSet, int lineBreakPos)
			throws IOException {
		in = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), charSet));
		CssCompressor compressor = new CssCompressor(in);
		out = new BufferedWriter(new FileWriter(file));
		compressor.compress(out, lineBreakPos);

		if (in != null) {
			in.close();
		}
		if (out != null) {
			out.close();
		}
	}

	private void compressJsFile(File file, String charSet, int lineBreakPos,
			boolean verbose, boolean munge, boolean preserveAllSemiColons,
			boolean disableOptimizations) throws IOException {
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), charSet));
			JavaScriptCompressor compressor = new JavaScriptCompressor(in,
					new ErrorReporter() {

						public void warning(String message, String sourceName,
								int line, String lineSource, int lineOffset) {
							if (line < 0) {
								System.err.println("\n[WARNING] " + message);
							} else {
								System.err.println("\n[WARNING] " + line + ':'
										+ lineOffset + ':' + message);
							}
						}

						public void error(String message, String sourceName,
								int line, String lineSource, int lineOffset) {
							if (line < 0) {
								System.err.println("\n[ERROR] " + message);
							} else {
								System.err.println("\n[ERROR] " + line + ':'
										+ lineOffset + ':' + message);
							}
						}

						public EvaluatorException runtimeError(String message,
								String sourceName, int line, String lineSource,
								int lineOffset) {
							error(message, sourceName, line, lineSource,
									lineOffset);
							return new EvaluatorException(message);
						}
					});
			out = new BufferedWriter(new FileWriter(file));
			compressor.compress(out, lineBreakPos, munge, verbose,
					preserveAllSemiColons, disableOptimizations);

		} catch (EvaluatorException e) {

			e.printStackTrace();
			// Return a special error code used specifically by the web
			// front-end.
			System.exit(2);

		}

		if (in != null) {
			in.close();
		}
		if (out != null) {
			out.close();
		}
	}
}
