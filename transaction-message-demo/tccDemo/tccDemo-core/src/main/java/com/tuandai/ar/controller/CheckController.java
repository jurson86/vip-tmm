package com.tuandai.ar.controller;

import com.tuandai.ar.domain.TModel;
import com.tuandai.ar.utils.FileUtils;
import com.tuandai.ar.utils.KryoSerializer;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Date;

@Controller
public class CheckController {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CheckController.class);



    @RequestMapping("/api/{info}")
    @ResponseBody
    public String check(@PathVariable("info") String info) {

        return info;
    }

    @RequestMapping("/api/bytefile")
    @ResponseBody
    public String bytefile() {
        KryoSerializer ks = new KryoSerializer();
        TModel tm = new TModel();
        tm.setA("a");
        tm.setB(1);
        tm.setC(new Date());

        try {
            byte[]  bb = ks.serialize(tm);
            TModel bcc1  = ks.deSerialize(bb,TModel.class);
            FileUtils.writeObj("D:\\java_app\\workspace\\tdw\\transaction-message-demo\\tccDemo\\logs\\lllll.txt",bb);
            LOGGER.info(bcc1.getA());
            File pfile = new File("D:\\java_app\\workspace\\tdw\\transaction-message-demo\\tccDemo\\logs\\lllll.txt");
            byte[]  bb2 = FileUtils.readObj(pfile);
            TModel bcc2 = ks.deSerialize(bb2,TModel.class);
            LOGGER.info(bcc2.getA());
        } catch (Exception ex) {
            LOGGER.info(ex.getMessage());
        }

        return "OK";
    }



	@RequestMapping("/api/thread")
    @ResponseBody
	public String thread() {


        RequestAttributes requestAttributes = null;
        try {
            requestAttributes = RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = requestAttributes == null ? null : ((ServletRequestAttributes) requestAttributes).getRequest();
            request.setAttribute("myheader","hello");

            LOGGER.info((String)request.getAttribute("myheader"));
            TestThread testThread = new TestThread();
            testThread.start();

        } catch (Throwable ex) {
            LOGGER.info(ex.getMessage());
        }

		return "OK";
	}




    class TestThread extends Thread {
        @Override
        public void run() {
            setName("test-thread");

            RequestAttributes requestAttributes1  = RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = requestAttributes1 == null ? null : ((ServletRequestAttributes) requestAttributes1).getRequest();

            LOGGER.info((String)request.getAttribute("myheader"));
        }

    }


}
