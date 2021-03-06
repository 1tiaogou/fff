package ff;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.FileUtils;
import utils.HttpUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppTest.class);

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);

    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        assertTrue(true);
        String iframeBg = "https://v.qq.com/iframe/player.html?vid=";
//        String iframeBg ="<iframe frameborder=\"0\" width=\"640\" height=\"498\" src=\"https://v.qq.com/iframe/player.html?vid=";
//        String iframeEnd ="&tiny=0&auto=0\" allowfullscreen></iframe>";
        StringBuilder mediaTag = new StringBuilder("<mediaID>");
        String mediaBg = "<audio src=\"";
        String mediaEnd = "\" controls autoplay></audio>";
        String iframeEnd = "&tiny=0&auto=0";
        String mediaSrc = "https://res.wx.qq.com/voice/getvoice?mediaid=";
        String link = "http://mp.weixin.qq.com/s?__biz=MjM5NzQ5MDg2MA==&mid=2650641446&idx=3&sn=a0239d5f8aa274b84e445b2b04051b43&chksm=bed00de589a784f38ee9574fd7157cc05289eb18c02042dcbe76af04824b218318f3ffaa0849#rd";
        String link2 = "https://mp.weixin.qq.com/s/LgeOd4GJLWkdWgVr6hr_og";
        String path = "C:/Users/admin/Desktop/sitemap02.html";
        String pathMac = "/Users/xmac/Desktop/sitemap02.html";

        String bg = "vid=";
        String en = "&width";
        String content = HttpUtils.get(link2);
        if (StringUtils.isBlank(content)) {
            LOGGER.warn("Link错误");
            return;
        }

        //     FileUtils.WriteStringToFile2(path,content);
        Document doc = Jsoup.parse(content);
        Element metaContent = doc.select("div#meta_content").first();
        Elements ems = metaContent.select("em ");
        System.out.println("作者" + ems.toString());
        System.out.println(ems.get(1).text());
        Elements aSource = metaContent.select("a");
        System.out.println("来源" + aSource.toString());
        //删除来源和作者
        doc.select("div#meta_content").first().remove();
        // 处理视频
        doc = manageMovie(doc);
        //处理图片
        doc = this.managePic(doc);

        //替换音频
        doc = manageAudio(doc, null);


        doc = this.manageUselessTag(doc);
        String docString = doc.toString();
        //   docString = docString.replaceAll("#js_content > p:nth-child(103) > strong > span","");
        FileUtils.WriteStringToFile2(pathMac, replaseOnString(docString));
    }

    //处理音频
    private Document manageAudio(Document doc, Long infoId) {
        final String mediaBg = "<audio src=\"https://res.wx.qq.com/voice/getvoice?mediaid=";
        final String mediaEnd = "\" controls autoplay></audio>";

        Elements medias = doc.select("mpvoice");
        medias.forEach(media -> {
            System.out.println(media.toString());
            String mediaId = media.attr("voice_encode_fileid");
            if (StringUtils.isNotBlank(mediaId)) {
                String audioName = media.attr("name");
                try {
                    audioName = URLDecoder.decode(audioName, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    LOGGER.warn("没有获取到音频name");
                }
                String mediaTags = mediaBg + mediaId + mediaEnd;
                media.wrap("<p></p>");
                Element p = media.parent();
                p.select("mpvoice").remove();
//                p.attr("align", "center");
                p.html(mediaTags);
                p.select("audio").attr("width", "80%");

            }
        });
        return doc;
    }

    //处理视频
    private Document manageMovie(Document doc) {
        final String iframeBg = "https://v.qq.com/iframe/player.html?vid=";
        final String iframeEnd = "&tiny=0&auto=0";
        final String bg = "vid=";
        final String en = "&width";
        Elements videos = doc.select("iframe");
        videos.forEach(video -> {
            String vi = video.attr("data-src");
            System.out.println("未替换" + video.toString());
            int begin = vi.indexOf(bg);
            int end = vi.indexOf(en);
            String vid = vi.substring(begin + bg.length(), end);
            String iframe = iframeBg + vid + iframeEnd;
            video.attr("src", iframe).
                    attr("width", "100%")
                    .attr("height", "auto")
                    .attr("allowfullscreen", "true");
        });
        return doc;
    }


    //处理图片
    private Document managePic(Document doc) {
        Elements imgs = doc.select("img");
        imgs.forEach(img -> {
            System.out.println(img.toString());
            String src = img.attr("data-src");
            img.attr("src", src);
        });
        return doc;
    }


    //删除无用标签
    private Document manageUselessTag(Document doc) {
        //删除无用标签
        Elements aTags = doc.select("a[href]");
        aTags.forEach(a -> {
            System.out.println("A标签" + a.toString());
            a.attr("href", "javascript:return false;");
            System.out.println("A标签" + a.toString());
            //  String p = a.text()
            //  a.wrap("<p></p>");
        });

        //删除script
        Elements scripts = doc.select("script");
        scripts.forEach(script -> {
            script.remove();
        });

        //处理style
        Elements styles = doc.getElementsByTag("style");
        styles.forEach(style -> {
            String styleString = style.toString().replace("font:inherit;", "").replace("font-size:100%;", "");
            style.wrap("<style></style>");
            Element newStyle = style.parent();
            style.remove();
            newStyle.html(styleString);
        });

        //处理body
        Elements bodys = doc.getElementsByTag("body");
        bodys.forEach(body -> {
            String bodyString = body.toString()
                    //.replace("body", "div")
                    .replace("data-src", "src")
                    .replace("http://", "https://")
                    .replace("?wx_fmt=jpeg", "")
                    .replace("?wx_fmt=png", "")
                    .replace("?wx_fmt=jpg", "")
                    .replace("?wx_fmt=gif", "");
            body.wrap("<body></body>");
            Element newStyle = body.parent();
            body.remove();
            newStyle.html(bodyString);
        });
        return doc;
    }


    //转为String 后做最后去除无用内容
    private String replaseOnString(String content) {
        String patt = "点击.*听课";
        String replacedContent = replacePattern(patt, content);
        return replacedContent.replace("<!--tailTrap<body></body><head></head><html></html>-->", "")
                .replace("<!--headTrap<body></body><head></head><html></html>-->", "");
    }

    //根据正则去除String内容
    private String replacePattern(String pattern, String content) {
        Pattern patt = Pattern.compile(pattern);
        Matcher m = patt.matcher(content);
        return m.replaceAll("");
    }
}