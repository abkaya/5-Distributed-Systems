package be.uantwerpen.group1.systemy.node;

import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;

/*
 * Tutorial for a folder watch service
 * https://docs.oracle.com/javase/tutorial/essential/io/notification.html
 */

/**
 * WatchService for a local folder, combined with the observable pattern to ease the
 * update process in replication.
 *
 * @author Abdil Kaya
 */
public class ObservableWatchService extends Observable {

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final boolean recursive;
    private boolean trace = false;
    private String fileName;
    private int action;

    private List<Observer> observers = new ArrayList<>();

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }



    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    ObservableWatchService(Path dir, boolean recursive) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey, Path>();
        this.recursive = recursive;

        if (recursive) {
            System.out.format("Scanning %s ...\n", dir);
            registerAll(dir);
            System.out.println("Done.");
        } else {
            register(dir);
        }

        // enable trace after initial registration
        this.trace = true;
    }
    
    /**
     * Process all events for keys queued to the watcher
     */
    void processEvents() {
        for (; ;) {
            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            //
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // Handle overflow event
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                //event context is the filename. E.g.: file.txt
                Path name = ev.context();
                //child is the directory relative to the folder we're watching. E.g. : test\file.txt
                Path child = dir.resolve(name);

                if (kind == ENTRY_CREATE) {
                    action = 1;
                    fileName = ev.context().toString();
                    //System.out.println("CREATE LOCAL FILE -> " + ev.context());
                    setChanged();
                    notifyObservers();
                    //This is where we want the new file method to be called by the replicator
                }
                if (kind == ENTRY_DELETE) {
                    action = 0;
                    fileName = ev.context().toString();
                    //System.out.println("DELETE LOCAL FILE -> " + ev.context());
                    setChanged();
                    notifyObservers();
                    //This is where we want deleted file method to be called by the replicator
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }
}