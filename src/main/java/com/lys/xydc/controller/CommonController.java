package com.lys.xydc.controller;

import com.lys.xydc.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * @author 陆玉升
 * date: 2023/04/2505/12
 * Description:
 * 文件上传和下载
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

  @Value("${xydc.path}")
  private String basePath;

  /**
   * 文件上传
   * @param file
   * @return
   */
  @PostMapping("/upload")
  public R<String> upload(MultipartFile file) {
    // file是一个临时文件，需要转存到指定位置,否则本次请求完成后临时文件会删除
    log.info(file.toString());

    // 获取文件的原始名称
    String originalFilename = file.getOriginalFilename(); //asdas.jpg

    // 获取文件的后缀
    String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

    // 使用UUID重新生成文件名，防止文件名重复造成文件覆盖
    String fileName = UUID.randomUUID().toString() + suffix;

    // 判断根目录是否存在
    File dir = new File(basePath);
    if (!dir.exists()) {
      // 目录不存在，则创建
      dir.mkdirs();
    }

    try {
      // 将临时文件转存到指定位置
      file.transferTo(new File(basePath + fileName));
    } catch (IOException e) {
      e.printStackTrace();
    }

    return R.success(fileName);
  }

  // 老师的方法
/*  @GetMapping("/download")
  public void download(String name, HttpServletResponse response) {
    FileInputStream is = null;
    ServletOutputStream os = null;
    try {
      // 输入流 通过输入流读取文件
      is = new FileInputStream(new File(basePath + name));
      // 输出流，通过输出流将文件写回浏览器，在浏览器中展示图片
      os = response.getOutputStream();

      response.setContentType("image/jpeg");

      int len = 0;
      byte[] buff = new byte[1024];
      while ((len = is.read(buff)) != -1) {
        os.write(buff, 0, len);
        os.flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if (os != null) {
        try {
          os.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }*/

  /**
   * 文件下载  使用ResponseEntity对象
   * @param name
   * @return
   */
  @GetMapping("/download")
  public ResponseEntity<byte[]> download(String name) {

    // 获取图片的路径
    String realPath = basePath + name;

    ResponseEntity<byte[]> responseEntity = null;

    // 创建输入流，读取文件
    FileInputStream is = null;
    try {
      is = new FileInputStream(realPath);
      // 创建字节数组
      byte[] buff = new byte[is.available()];
      // 将流读到字节数组
      is.read(buff);
      // 创建HttpHeaders对象设置响应头信息
      MultiValueMap<String, String> headers = new HttpHeaders();
      headers.add("Content-Type", "image/jpeg");
      // 设置响应状态码
      HttpStatus statusCode = HttpStatus.OK;
      // 创建ResponseEntity对象
      responseEntity = new ResponseEntity<>(buff, headers, statusCode);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return responseEntity;
  }


}
