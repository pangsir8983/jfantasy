package org.jfantasy.framework.lucene.cache;

import org.jfantasy.framework.error.IgnoreException;
import org.jfantasy.framework.lucene.BuguIndex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IndexWriterCache {
	private static final Log LOGGER = LogFactory.getLog(IndexWriterCache.class);

	private static IndexWriterCache instance = new IndexWriterCache();
	private Map<String, IndexWriter> cache;

	private IndexWriterCache() {
		this.cache = new ConcurrentHashMap<String, IndexWriter>();
	}

	public static IndexWriterCache getInstance() {
		return instance;
	}

    public IndexWriter get(String name) {
        if (this.cache.containsKey(name)) {
            return this.cache.get(name);
        }
        synchronized (this) {
            if (this.cache.containsKey(name)) {
                return this.cache.get(name);
            }
            BuguIndex index = BuguIndex.getInstance();
            IndexWriterConfig cfg = new IndexWriterConfig(index.getVersion(), index.getAnalyzer());
            cfg.setRAMBufferSizeMB(index.getBufferSizeMB());
            try {
                Directory dir = FSDirectory.open(BuguIndex.getInstance().getOpenFolder("/" + name + "/"));
                if (IndexWriter.isLocked(dir)) {
                    IndexWriter.unlock(dir);
                }
                cfg.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
                this.cache.put(name, new IndexWriter(dir, cfg));
                return this.cache.get(name);
            } catch (IOException ex) {
                LOGGER.error("Something is wrong when create IndexWriter for " + name, ex);
                throw new IgnoreException(ex.getMessage(), ex);
            }
        }
    }

    public Map<String, IndexWriter> getAll() {
        return this.cache;
    }

}