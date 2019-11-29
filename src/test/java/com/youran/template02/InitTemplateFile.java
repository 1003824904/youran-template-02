package com.youran.template02;

import com.youran.common.util.JsonUtil;
import com.youran.generate.constant.ContextType;
import com.youran.generate.pojo.po.CodeTemplatePO;
import com.youran.generate.pojo.po.TemplateFilePO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * 初始化代码模板
 * <p>
 * 使用场景：
 * <ul><li>1、首次构建vue前端脚手架模板时使用
 * <li>2、vue-admin-template项目有更新时，拉取最新代码
 * （需要在template-02-init分支上做该操作）
 * </ul>
 *
 * @author cbb
 * @date 2019/11/20
 */
public class InitTemplateFile {

    public static final String REMOTE_URL = "https://github.com/PanJiaChen/vue-admin-template.git";
    public static final String BRANCH_TO_CLONE = "master";
    public static final String RESOURCES_DIR = "D:/workspace_cbb/youran-template-02/src/main/resources";
    public static final String FTL_DIR = RESOURCES_DIR + "/ftl";
    public static final String TEMPLATE_JSON_FILE_PATH = RESOURCES_DIR + "/template.json";
    public static final String[] ASSETS_EXTS = {
        "ico",
        "png",
        "svg"
    };
    public static final String[] BINARY_FILE_NAME = {
    };
    public static final String[] IGNORE_FILE_PATH_PREFIX = {
        "/src/views/dashboard",
        "/src/views/form",
        "/src/views/nested",
        "/src/views/table",
        "/src/views/tree"
    };

    private CodeTemplatePO templatePO;

    /**
     * 初始化模板对象
     */
    @Before
    public void initTemplatePO() {
        templatePO = new CodeTemplatePO();
        templatePO.setCode("youran-template-02");
        templatePO.setName("标准vue前端模板");
        templatePO.setTemplateVersion("1.0.0");
        templatePO.setSysLowVersion("3.0.0");
        templatePO.setSysDefault(true);
        templatePO.setRemark("标准vue前端模板");
        templatePO.setTemplateFiles(new ArrayList<>(100));
    }


    @Test
    public void initCode() throws Exception {
        // 从github下载vue-admin-template原始代码
        //File repoDir = this.checkOutVueAdminTemplate();
        File repoDir = new File("C:\\Users\\caibi\\AppData\\Local\\Temp\\vue-admin-template8463170824386821771");
        // 清空模板目录
        this.cleanFtlDir();
        // 遍历原始代码，并拷贝模板文件
        this.onEachRepoFile(repoDir, repoFile -> this.copyRepoFile(repoDir, repoFile));
        // 模板元数据写入template.json
        JsonUtil.writeJsonToFile(templatePO, true, new File(TEMPLATE_JSON_FILE_PATH));
    }


    /**
     * 从github下载vue-admin-template原始代码
     */
    private File checkOutVueAdminTemplate() throws Exception {
        // 创建临时文件，并删除该文件，通过该方式防止文件夹已经被占用
        File repoDir = File.createTempFile("vue-admin-template", "");
        repoDir.delete();

        System.out.println("下载github原始代码到该目录:" + repoDir.getPath());
        Git.cloneRepository()
            .setURI(REMOTE_URL)
            .setCloneAllBranches(false)
            .setBranchesToClone(Arrays.asList(BRANCH_TO_CLONE))
            .setDirectory(repoDir)
            .call();
        return repoDir;
    }

    /**
     * 清空模板目录
     *
     * @throws Exception
     */
    @Test
    public void cleanFtlDir() throws Exception {
        File ftlDir = new File(FTL_DIR);
        FileUtils.cleanDirectory(ftlDir);
        // 模板元数据写入template.json
        JsonUtil.writeJsonToFile(templatePO, true, new File(TEMPLATE_JSON_FILE_PATH));
    }

    /**
     * 遍历每个源码文件
     *
     * @param dir
     */
    private void onEachRepoFile(File dir, Consumer<File> consumer) {
        File[] files = dir.listFiles(getFileFilter());
        if (ArrayUtils.isEmpty(files)) {
            return;
        }
        for (File file : files) {
            if (file.isHidden()) {
                continue;
            }
            if (file.isDirectory()) {
                this.onEachRepoFile(file, consumer);
            } else {
                consumer.accept(file);
            }
        }
    }

    /**
     * 获取文件过滤器
     *
     * @return
     */
    public static FileFilter getFileFilter() {
        return HiddenFileFilter.VISIBLE;
    }

    /**
     * 将源码文件拷贝成freemarker模板文件
     *
     * @param repoDir
     * @param repoFile
     */
    private void copyRepoFile(File repoDir, File repoFile) {
        String path = repoFile.getPath().substring(repoDir.getPath().length())
            .replaceAll("\\\\", "/");
        System.out.println(path);
        if (this.isRepoFileIgnore(path)) {
            return;
        }
        String target = FTL_DIR + path;
        try {
            if (!this.isBinaryFile(repoFile)) {
                String content = FileUtils.readFileToString(repoFile, "utf-8");
                FileUtils.write(new File(target + ".ftl"),
                    this.convertFtlContent(content), "utf-8");
                this.buildAndSetTemplateFilePO(path + ".ftl", false);
            } else {
                FileUtils.copyFile(repoFile, new File(target));
                this.buildAndSetTemplateFilePO(path, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }


    /**
     * 转换模板内容
     * @param content
     * @return
     */
    private String convertFtlContent(String content) {
        return content.replaceAll("\\$\\{","\\${r'\\$'}{")
            .replaceAll("#\\{","\\${r'#'}{");
    }

    /**
     * 判断是否需要忽略源码文件
     *
     * @param repoFilePath
     * @return
     */
    private boolean isRepoFileIgnore(String repoFilePath) {
        return Arrays.stream(IGNORE_FILE_PATH_PREFIX)
            .anyMatch(prefix -> repoFilePath.startsWith(prefix));
    }

    /**
     * 判断源码文件是否二进制文件
     *
     * @param repoFile
     * @return
     */
    private boolean isBinaryFile(File repoFile) {
        String name = repoFile.getName();
        if (ArrayUtils.contains(BINARY_FILE_NAME, name)) {
            return true;
        }
        String extension = FilenameUtils.getExtension(name);
        if (ArrayUtils.contains(ASSETS_EXTS, StringUtils.lowerCase(extension))) {
            return true;
        }
        return false;
    }

    /**
     * 创建并设置模板文件对象
     *
     * @param path   文件路径
     * @param binary 是否二进制文件
     */
    private void buildAndSetTemplateFilePO(String path, boolean binary) {
        String fileName = path.substring(path.lastIndexOf("/") + 1);

        String dir = path.substring(0, path.lastIndexOf("/"));
        if (StringUtils.isBlank(dir)) {
            dir = "/";
        }
        TemplateFilePO filePO = new TemplateFilePO();
        filePO.setFileName(fileName);
        filePO.setFileDir(dir);
        filePO.setContextType(ContextType.GLOBAL.getValue());
        filePO.setAbstracted(false);
        filePO.setBinary(binary);

        templatePO.getTemplateFiles().add(filePO);
    }


}
