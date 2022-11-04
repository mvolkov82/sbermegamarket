package com.okeandra.demo.controllers;

import com.okeandra.demo.services.processing.FileInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

@org.springframework.web.bind.annotation.RestController
public class RestController {
    private FileInfoService fileInfoService;

    @Autowired
    public RestController(FileInfoService fileInfoService) {
        this.fileInfoService = fileInfoService;
    }

    @GetMapping("/query/1")
    public String ajaxQuery1() {
//        System.out.println("YAHOOOOOOOOOOOOOO");
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        String date1CItemsXls = fileInfoService.getExcelSourceFeedDate();
        System.out.println("query/1 date1CItemsXls = " + date1CItemsXls);


        return date1CItemsXls;
    }

    @GetMapping("/query/2")
    public String ajaxQuery2() {
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        String date1CItemsFeedXLS = fileInfoService.getExcelWithLimitsDate();
        System.out.println("/query/2 date1CItemsFeedXLS="+date1CItemsFeedXLS);
        return date1CItemsFeedXLS;
    }

    @GetMapping("/query/3")
    public String ajaxQuery3() {
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println("Daaaaaa");
        String basicFeedForSberDate = fileInfoService.getXmlBasicFeedDate();
        System.out.println("/query/3 basicFeedForSberDate=" + basicFeedForSberDate);
        return basicFeedForSberDate;
    }

    @GetMapping("/query/4")
    public String ajaxQuery4() {
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println("Suuuuuuper");
        String dutyFreeFeedForSberDate = fileInfoService.getXmlDutyFreeFeedDate();
        System.out.println("/query/4 dutyFreeFeedForSberDate=" + dutyFreeFeedForSberDate);
        return dutyFreeFeedForSberDate;
    }

    @GetMapping("/query/5")
    public String ajaxQuery5() {
//        System.out.println("PRRRRRRRRIMMAAAAAAAAA");
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        String dayPerDayFileDate = fileInfoService.getDayPerDayFileDate();
        System.out.println("/query/5 dayPerDayFileDate=" + dayPerDayFileDate);
        return dayPerDayFileDate;
    }

    @GetMapping("/query/6")
    public String ajaxQuery6() {
//        System.out.println("FANTASTISSSSSSSSSSSHHHHHH");
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        String shipmentFromSberWarehouseFeedDate = fileInfoService.getShipmentFormSberWarehouseFeedDate();
        System.out.println("/query/6 shipmentFromSberWarehouseFeedDate=" + shipmentFromSberWarehouseFeedDate);
        return shipmentFromSberWarehouseFeedDate;
    }

    @GetMapping("/query/7")
    public String ajaxQuery7() {
//        System.out.println("BEAUTYFUUUUUUUL");
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        String itemsFromSberWarehouseFileDate = fileInfoService.getItemsShipmentFormSberWarehouseFileDate();
        System.out.println("/query/7 itemsFromSberWarehouseFileDate=" + itemsFromSberWarehouseFileDate);
        return itemsFromSberWarehouseFileDate;
    }

    @GetMapping("/query/ozon1")
    public String ajaxOzon1() {
        String ozonProductsFileDate = fileInfoService.getOzonProductsFileDate();
        return ozonProductsFileDate;
    }

    @GetMapping("/query/ozon2")
    public String ajaxOzon2() {
        String ozonFeedFileDate = fileInfoService.getOzonFeedFileDate();
        return ozonFeedFileDate;
    }

    @GetMapping("/query/groupprice")
    public String ajaxGroupPrice() {
        String grouppriceDateTime = fileInfoService.getGrouppriceFileDate();
        return grouppriceDateTime;
    }

    @GetMapping("/query/selvis")
    public String ajaxSelvis() {
        String grouppriceDateTime = fileInfoService.getSelvisFileDate();
        return grouppriceDateTime;
    }

    @GetMapping("/query/yandex")
    public String ajaxYandex() {
        String dateTime = fileInfoService.getYandexFileDate();
        return dateTime;
    }
}
