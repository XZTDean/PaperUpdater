package me.deanx.paperupdater;

import org.apache.commons.cli.*;

public class CommandLineMain {
    public static void main(String[] args) {
        final Option LIST_INFO = new Option("l", "list", false, "List the build or download url");
        final Option BUILD = new Option("b", "build", true, "Specify the build number");
        final Option VERSION = new Option("v", "version", true, "Specify the minecraft version");
        final Option VERSION_FAMILY = new Option("f", "family", true, "Specify the minecraft version family");
        final Option OUTPUT = new Option("o", "output", true, "Output file name and path");

        Options options = new Options();
        options.addOption(LIST_INFO);
        options.addOption(BUILD);
        options.addOption(VERSION);
        options.addOption(VERSION_FAMILY);
        options.addOption(OUTPUT);

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Parsing failed.\n" + e.getLocalizedMessage());
            return;
        }

        commandLine.hasOption(LIST_INFO);
        Downloader downloader = new Downloader();
    }
}
