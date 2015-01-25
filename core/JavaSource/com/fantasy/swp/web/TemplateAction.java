package com.fantasy.swp.web;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.fantasy.framework.dao.Pager;
import com.fantasy.framework.dao.hibernate.PropertyFilter;
import com.fantasy.framework.struts2.ActionSupport;
import com.fantasy.swp.bean.DataInferface;
import com.fantasy.swp.bean.Template;
import com.fantasy.swp.service.DataInferfaceService;
import com.fantasy.swp.service.TemplateService;

/**
 *@Author lsz
 *@Date 2014-2-17 下午2:27:11
 *
 */
public class TemplateAction extends ActionSupport {
	
	private static final long serialVersionUID = -2384754979431858323L;
	
	@Resource(name="swp.page.templateService")
	private TemplateService templateService;
    @Resource
    private DataInferfaceService dataInferfaceService;
	/**
	 * 首页
	 * @return
	 */
	public String index(){
		this.search(new Pager<Template>(),new ArrayList<PropertyFilter>());
		this.attrs.put("pager", this.attrs.get(ROOT));
		this.attrs.remove(ROOT);
		return SUCCESS;
	}
	
	/**
	 * 搜索
	 * @param pager
	 * @param filters
	 * @return
	 */
	public String search(Pager<Template> pager,List<PropertyFilter> filters){
        Pager<Template> pages = this.templateService.findPager(pager, filters);
        System.out.println(pages.getPageItems().size());
        System.out.println(pages.getPageItems().get(0).getContent());
		this.attrs.put(ROOT,this.templateService.findPager(pager, filters));
		return JSONDATA;
	}
	
	/**
	 * 保存
	 * @param template
	 * @return
	 * @throws IOException
	 */
	public String save(Template template) throws IOException{
		//,File file,String fileContentType, String fileFileName
		//Directory directory = fileService.getDirectory("template");
		//UploadFileManager fileManager = FileManagerFactory.getInstance().getUploadFileManager(directory.getFileManager().getId());
		//template.setFileDetail(fileManager.writeFile(directory.getDirPath(), file, fileContentType, fileFileName));
        InputStream in = request.getInputStream();
        StringBuilder fileSb = new StringBuilder("");
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String tmpStr = "";
            while ((tmpStr = bufferedReader.readLine()) != null) {
                fileSb.append(tmpStr);
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        template.setContent(fileSb.toString());
		this.templateService.save(template);
        if(template.getDataInferfaces()!=null){
            for(DataInferface dataInferface : template.getDataInferfaces()){
                dataInferface.setTemplate(template);
                this.dataInferfaceService.save(dataInferface);
            }
        }
		return JSONDATA;
	}
	/**
	 * 修改
	 * @param id
	 * @return
	 */
	public String edit(Long id){
		this.attrs.put("template", this.templateService.get(id));
		return SUCCESS;
	}
	
	public String delete(Long[] ids){
        System.out.println("delete.....");
		this.templateService.delete(ids);
		return JSONDATA;
	}
	
}

