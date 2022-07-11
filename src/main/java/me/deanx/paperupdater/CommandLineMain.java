package me.deanx.paperupdater;

import org.apache.commons.cli.*;

public class CommandLineMain {
    public static void main(String[] args) {
        final Option LIST_INFO = new Option("l", "list", false, "List the build or download url");
        final Option BUILD = new Option("b", "build", true, "Specify the build number");
        final Option VERSION = new Option("v", "version", true, "Specify the minecraft version");
        final Option VERSION_FAMILY = new Option("f", "family", true, "Specify the minecraft version family");
        final Option OUTPUT = new Option("o", "output", true, "Output file name and path");
        final Option INTERACT = new Option("i", "interact", false, "Interact mode");
        final Option HELP = new Option("h", "help", false, "Help page");

        Options options = new Options();
        options.addOption(LIST_INFO);
        options.addOption(BUILD);
        options.addOption(VERSION);
        options.addOption(VERSION_FAMILY);
        options.addOption(OUTPUT);
        options.addOption(INTERACT);
        options.addOption(HELP);

        CommandLineParser parser = new DefaultParser(false);
        CommandLine commandLine;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Parsing failed.\n" + e.getLocalizedMessage());
            return;
        }

        if (commandLine.hasOption(HELP)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("PaperUpdate", options);
            return;
        }
        DownloadController downloader = new DownloadController();
        if (commandLine.hasOption(OUTPUT)) {
            downloader.setOutputFile(commandLine.getOptionValue(OUTPUT));
        }
        if (commandLine.hasOption(VERSION)) {
            downloader.setVersion(commandLine.getOptionValue(VERSION));
        }
        if (commandLine.hasOption(VERSION_FAMILY)) {
            downloader.setVersionFamily(commandLine.getOptionValue(VERSION_FAMILY));
        }
        if (commandLine.hasOption(BUILD)) {
            downloader.setBuild(Integer.parseInt(commandLine.getOptionValue(BUILD)));
        }

        if (commandLine.hasOption(LIST_INFO)) {
            System.out.println("The download URL for version " + downloader.getCalculatedVersion()
                    + " build " + downloader.getCalculatedBuild() + " is:\n" + downloader.getUrl());
        } else {
            downloader.download();
        }
    }
}
