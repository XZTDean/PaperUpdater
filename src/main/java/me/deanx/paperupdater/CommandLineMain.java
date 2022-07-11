package me.deanx.paperupdater;

import org.apache.commons.cli.*;

import java.util.Scanner;

public class CommandLineMain {
    public static void main(String[] args) throws ParseException {
        CommandLineMain main = new CommandLineMain(args);

        if (main.commandLine.hasOption(HELP)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("PaperUpdate", options);
            return;
        }

        DownloadController downloader;
        if (main.commandLine.hasOption(INTERACT)) {
            downloader = main.getDownloadControllerFromInput();
        } else {
            downloader = main.getDownloadControllerFromArgs();
        }

        if (main.isDownload) {
            downloader.download();
        } else {
            System.out.println("The download URL for version " + downloader.getCalculatedVersion()
                    + " build " + downloader.getCalculatedBuild() + " is:\n" + downloader.getUrl());
        }
    }

    private CommandLineMain(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser(false);
        commandLine = parser.parse(options, args);
    }

    private DownloadController getDownloadControllerFromArgs() {
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
            isDownload = false;
        }
        return downloader;
    }

    private DownloadController getDownloadControllerFromInput() {
        DownloadController downloader = new DownloadController();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Minecraft Version: ");
        downloader.setVersion(scanner.nextLine());
        System.out.println("Minecraft Version Family: ");
        downloader.setVersionFamily(scanner.nextLine());
        System.out.println("Build (blank for latest): ");
        String build = scanner.nextLine();
        if (!build.isBlank()) {
            int buildNum = Integer.parseInt(build);
            downloader.setBuild(buildNum);
        }
        System.out.println("Operation (0: Download, 1: Display Info): ");
        if (scanner.nextInt() == 1) {
            isDownload = false;
        }
        if (isDownload) {
            System.out.println("Output File Location: ");
            downloader.setOutputFile(scanner.nextLine());
        }
        scanner.close();
        return downloader;
    }

    private final CommandLine commandLine;
    private boolean isDownload = true;

    private static final Options options = new Options();

    private static final Option LIST_INFO = new Option("l", "list", false, "List the build or download url");
    private static final Option BUILD = new Option("b", "build", true, "Specify the build number");
    private static final Option VERSION = new Option("v", "version", true, "Specify the minecraft version");
    private static final Option VERSION_FAMILY = new Option("f", "family", true, "Specify the minecraft version family");
    private static final Option OUTPUT = new Option("o", "output", true, "Output file name and path");
    private static final Option INTERACT = new Option("i", "interact", false, "Interact mode");
    private static final Option HELP = new Option("h", "help", false, "Help page");

    static {
        options.addOption(LIST_INFO);
        options.addOption(BUILD);
        options.addOption(VERSION);
        options.addOption(VERSION_FAMILY);
        options.addOption(OUTPUT);
        options.addOption(INTERACT);
        options.addOption(HELP);
    }
}
