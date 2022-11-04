package com.okeandra.demo.controllers;

import java.util.List;
import java.util.Random;

import com.okeandra.demo.services.processing.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class WebController {
    @Autowired
    private AllFeedsStarter allFeedsStarter;
    @Autowired
    private FeedInsales feedInsales;
    @Autowired
    private SberFeedAll0DayKapousOllinOneDay sberFeedAll0DayKapousOllinOneDay;
    @Autowired
    private FeedSberDutyFree feedSberDutyFree;
    @Autowired
    private FeedGroupPrice feedGroupPrice;
    @Autowired
    private FeedSelvis feedSelvis;
    @Autowired
    private FeedOzon feedOzon;
    @Autowired
    private OzonUploadService ozonUploadService;
    @Autowired
    private FeedSberFromSber deliveryFromSber;
    @Autowired
    private FeedYandex feedYandex;

//
//    public WebController(AllFeedsStarter allFeedsStarter, SberFromSberFeed deliveryFromSber, InsalesFeed insalesFeed, SberDutyFreeFeed sberDutyFreeFeed, GroupPriceFeed groupPriceFeed, SelvisFeed selvisFeed, SberFeedAll0DayKapousOllinOneDay sberFeedAll0DayKapousOllinOneDay, OzonFeed ozonFeed, OzonUploadService ozonUploadService) {
//        this.allFeedsStarter = allFeedsStarter;
//        this.deliveryFromSber = deliveryFromSber;
//        this.insalesFeed = insalesFeed;
//        this.sberDutyFreeFeed = sberDutyFreeFeed;
//        this.groupPriceFeed = groupPriceFeed;
//        this.selvisFeed = selvisFeed;
//        this.sberFeedAll0DayKapousOllinOneDay = sberFeedAll0DayKapousOllinOneDay;
//        this.ozonFeed = ozonFeed;
//        this.ozonUploadService = ozonUploadService;
//    }

    @GetMapping("/")
    public String indexPage(Model model) {
        //For no cashing URL
        Random random = new Random();
        model.addAttribute("randomVal", random.nextInt(1000));
        return "index";
    }

    //Установка безопасного остатка
    @GetMapping("/create-feed-okeandra")
    public String createXlsFeedForOkeandra(Model model) {
        List<String> log = feedInsales.start();
        model.addAttribute("result", log);
        return "result";
    }


    // Из-за херовых сроков пока делаем на все 0 дней кроме Проф.
    @GetMapping("/create-feed-sber")
    public String createFeedForSber(Model model) {
        List<String> log = sberFeedAll0DayKapousOllinOneDay.start();
        model.addAttribute("result", log);
        return "result";
    }


    @GetMapping("/create-sber-from-sber")
    public String createFeedForShipmentFromSberWarehouse(Model model) {
        List<String> log = deliveryFromSber.start();
        model.addAttribute("result", log);
        return "result";
    }


    @GetMapping("/create-sber-duty")
    public String createFeedForSberDutyFree(Model model) {
        List<String> log = feedSberDutyFree.start();
        model.addAttribute("result", log);
        return "result";
    }


    @GetMapping("/create-ozon-feed")
    public String createOzonFeed(Model model) {
        List<String> log = feedOzon.start();
        model.addAttribute("result", log);
        return "result";
    }

    @GetMapping("/create-groupprice")
    public String createGroupPriceFeed(Model model) {
        List<String> log = feedGroupPrice.start();
        model.addAttribute("result", log);
        return "result";
    }

    @GetMapping("/create-selvis")
    public String createSelvisFeed(Model model) {
        List<String> log = feedSelvis.start();
        model.addAttribute("result", log);
        return "result";
    }

    @GetMapping("/create-yandex")
    public String createYandexFeed(Model model) {
        List<String> log = feedYandex.start();
        model.addAttribute("result", log);
        return "result";
    }

    @GetMapping("/start_all")
    public String startAll(Model model) {
        List<String> log = allFeedsStarter.start();
        model.addAttribute("result", log);
        return "result";
    }

    @PostMapping("/upload-ozon-items")
    public String singleFileUpload(@RequestParam("file") MultipartFile file,
                                   Model model) {

        String log = ozonUploadService.uploadFileAndSendInFtp(file);
        model.addAttribute("result", log);

        return "result";
    }


}
