package org.jfantasy.file.service;

import org.jfantasy.file.bean.FileDetailKey;
import org.jfantasy.file.bean.FilePart;
import org.jfantasy.file.dao.FilePartDao;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
@Transactional
public class FilePartService {

    @Autowired
    private FilePartDao filePartDao;

    public void save(FileDetailKey key, String entireFileHash, String partFileHash, Integer total, Integer index) {
        if (this.findByPartFileHash(entireFileHash, partFileHash) != null) {
            return;
        }
        FilePart part = new FilePart();
        part.setAbsolutePath(key.getAbsolutePath());
        part.setFileManagerId(key.getFileManagerId());
        part.setEntireFileHash(entireFileHash);
        part.setPartFileHash(partFileHash);
        part.setTotal(total);
        part.setIndex(index);
        this.filePartDao.save(part);
    }

    public void delete(FileDetailKey key) {
        this.filePartDao.delete(key);
    }

    public List<FilePart> find(String entireFileHash) {
        return this.filePartDao.find(new Criterion[]{Restrictions.eq("entireFileHash", entireFileHash)}, "index", "asc");
    }

    public FilePart findByPartFileHash(String entireFileHash, String partFileHash) {
        return this.filePartDao.findUnique(Restrictions.eq("entireFileHash", entireFileHash), Restrictions.eq("partFileHash", partFileHash));
    }
}
