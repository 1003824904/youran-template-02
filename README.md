# vue前端代码模板

## 介绍

本项目是<a href="../../../youran" target="_blank">youran代码自动化平台</a>的预置代码模板：
**vue前端模板**

## 软件架构

将开源vue管理后台脚手架：
<a href="https://github.com/PanJiaChen/vue-admin-template" target="_blank">vue-admin-template</a>
包装成代码模板，所有前端技术细节请参考 <a href="https://panjiachen.github.io/vue-element-admin-site/zh/guide/" target="_blank">vue-element-admin手册</a>


## 安装教程

1.  首先保证本地maven仓库中已经安装了youran代码生成器的核心jar包
2.  用package命令直接编译打包本项目
3.  打包完之后生成的youran-template-02-x.x.x-SNAPSHOT-src.zip
就能在youran平台中导入了

## 生成的前端工程支持两种开发模式

```

# 前端独立启动，自动mock后端服务
npm run dev

# 配合生成的后端服务，前后联调（需要先启动后端服务）
npm run dev:joint

```
