package com.fantasy.file.service;

import com.fantasy.file.FileManager;
import com.fantasy.file.bean.Directory;
import com.fantasy.file.bean.FileDetail;
import com.fantasy.file.bean.FileDetailKey;
import com.fantasy.file.bean.FilePart;
import com.fantasy.file.manager.UploadFileManager;
import com.fantasy.framework.spring.SpELUtil;
import com.fantasy.framework.util.common.*;
import com.fantasy.framework.util.common.file.FileUtil;
import com.fantasy.framework.util.web.WebUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class FileUploadService {

    private static final Log logger = LogFactory.getLog(FileUploadService.class);

    private final static String separator = "/";
    @Resource
    private transient FileService fileService;
    @Resource
    private transient FilePartService filePartService;

    /**
     * 文件上传方法
     * <br/>dir 往下为分段上传参数
     *
     * @param attach            附件信息
     * @param contentType 附件类型
     * @param fileName    附件名称
     * @param dir               附件上传目录Id
     * @param entireFileName    完整文件名称
     * @param entireFileDir     附件完整文件的上传目录信息
     * @param entireFileHash    文件hash值
     * @param partFileHash      分段文件的hash值
     * @param total             分段上传时的总段数
     * @param index             当前片段
     * @return FileDetail
     * @throws IOException
     */
    public FileDetail upload(File attach, String contentType, String fileName, String dir, String entireFileName, String entireFileDir, String entireFileHash, String partFileHash, Integer total, Integer index) throws IOException {
        //判断是否为分段上传
        boolean isPart = StringUtil.isNotBlank(entireFileHash) && StringUtil.isNotBlank(partFileHash) && StringUtil.isNotBlank(entireFileName) && StringUtil.isNotBlank(entireFileDir) && ObjectUtil.isNotNull(total) && StringUtil.isNotNull(index);
        //生成分段上传的文件名
        if (isPart && "blob".equalsIgnoreCase(fileName)) {
            fileName = entireFileName + ".part" + StringUtil.addZeroLeft(index.toString(), total.toString().length());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("上传文件参数:{fileName:" + contentType + ",contentType:" + fileName + ",dir:" + dir + ",isPart:" + isPart + "}");
        }

        //上传文件信息
        FileDetail fileDetail;

        if (isPart) {//如果为分段上传
            //获取文件上传目录的配置信息
            Directory directory = fileService.getDirectory(dir);
            FileManager fileManager = FileManagerFactory.getInstance().getUploadFileManager(directory.getFileManager().getId());

            FilePart filePart = filePartService.findByPartFileHash(entireFileHash, partFileHash);
            if (filePart == null || (fileDetail = fileService.get(FileDetailKey.newInstance(filePart.getAbsolutePath(), filePart.getFileManagerId()))) == null) {//分段已上传信息
                fileDetail = this.upload(attach, contentType, fileName, dir);
                filePartService.save(FileDetailKey.newInstance(fileDetail.getAbsolutePath(), fileDetail.getFileManagerId()), entireFileHash, partFileHash, total, index);
            }
            //查询上传的片段
            List<FilePart> fileParts = filePartService.find(entireFileHash);
            FilePart _part = ObjectUtil.remove(fileParts, "index", 0);
            if (_part == null) {
                List<FilePart> joinFileParts = new ArrayList<FilePart>();
                ObjectUtil.join(joinFileParts, fileParts, "index");

                if (joinFileParts.size() == total) {
                    //临时文件
                    File tmp = FileUtil.tmp();
                    //合并 Part 文件
                    FileOutputStream out = new FileOutputStream(tmp);
                    for (FilePart _filePart : joinFileParts) {
                        InputStream in = fileManager.readFile(_filePart.getAbsolutePath());
                        StreamUtil.copy(in, out);
                        StreamUtil.closeQuietly(in);
                        fileManager.removeFile(_filePart.getAbsolutePath());
                        ObjectUtil.remove(fileParts, SpELUtil.getExpression(" absolutePath == #value.getAbsolutePath() and fileManagerId == #value.getFileManagerId() "), filePart);
                    }
                    StreamUtil.closeQuietly(out);

                    //保存合并后的新文件
                    fileDetail = this.upload(tmp, contentType, entireFileName, entireFileDir);

                    //删除临时文件
                    FileUtil.delFile(tmp);

                    //删除 Part 文件
                    for (FilePart _filePart : fileParts) {
                        fileManager.removeFile(_filePart.getAbsolutePath());
                    }

                    //在File_PART 表冗余一条数据 片段为 0
                    filePartService.save(FileDetailKey.newInstance(fileDetail.getAbsolutePath(), fileDetail.getFileManagerId()), entireFileHash, entireFileHash, total, 0);
                }
            } else {
                //删除 Part 文件
                for (FilePart _filePart : fileParts) {
                    fileManager.removeFile(_filePart.getAbsolutePath());
                }
            }
        } else {
            fileDetail = this.upload(attach, contentType, fileName, dir);
        }
        return fileDetail;
    }

    public FileDetail upload(File attach, String contentType, String fileName, String dir) throws IOException {
        Directory directory = this.fileService.getDirectory(dir);
        //获取文件Md5码
        String md5 = MessageDigestUtil.getInstance().get(attach);// 获取文件MD5
        // 获取虚拟目录
        String absolutePath = directory.getDirPath() + separator + StringUtil.uuid() + "." + WebUtil.getExtension(fileName);
        // 文件类型
        FileDetail fileDetail;
        // 获取真实目录
        String realPath = "";

        String fileManagerId = directory.getFileManager().getId();

        FileManager fileManager = FileManagerFactory.getInstance().getFileManager(fileManagerId);
        if (fileManager instanceof UploadFileManager) {
            UploadFileManager uploadFileManager = ((UploadFileManager) fileManager);
            fileDetail = fileService.getFileDetailByMd5(md5, fileManagerId);
            if (fileDetail == null) {
                realPath = separator + "files" + separator + DateUtil.format("yyyy-MM-dd") + separator + md5;
                uploadFileManager.writeFile(realPath, attach);
            } else {
                realPath = fileDetail.getRealPath();
            }
        } else {
            fileManager.writeFile(absolutePath, attach);
        }
        return fileService.saveFileDetail(absolutePath, fileName, contentType, attach.length(), md5, realPath, fileManagerId, "");
    }

}
