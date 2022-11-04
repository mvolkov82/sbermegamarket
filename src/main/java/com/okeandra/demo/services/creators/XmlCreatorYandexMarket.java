package com.okeandra.demo.services.creators;

import com.okeandra.demo.models.Offer;
import com.okeandra.demo.models.OfferYandex;
import com.okeandra.demo.models.YmlObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Component
@Qualifier("xmlYandexMarket")
public class XmlCreatorYandexMarket extends XmlCreator {
    @Override
    public String getBodyAsXML(YmlObject ymlObject) {
        Document document = getDocument(ymlObject);
        Element root = document.createElement("offers");
        document.appendChild(root);

            /*
            <offer type="vendor.model" available="true" id="26605093">
            <url>https://okeandra.ru/product/vitex-aloe-vera-krem-dlya-ruk-pitatelnyy-150-ml</url>
            <price>196.0</price>
            <currencyId>RUB</currencyId>
            <categoryId>2670899</categoryId>
            <picture>https://static-ru.insales.ru/images/products/1/1056/514221088/AloeV44.jpg</picture>
            <picture>https://static-ru.insales.ru/images/products/1/1367/514229591/Vitex_AloeVera_KremRukiPitat.jpg</picture>
            <store>true</store>
            <pickup>true</pickup>
            <delivery>true</delivery>
            <vendor>Витекс</vendor>
            <vendorCode>AUT00000004</vendorCode>
            <barcode>4810153010568</barcode>
            <model>"Aloe Vera" Крем для рук ''Питательный'' 150 мл (Витекс)</model>
            <description>Уникальный по составу крем интенсивно питает, смягчает и разглаживает кожу рук. Сок Алоэ и витамин Е восстанавливают оптимальный баланс влаги в коже, придавая ей мягкость и нежность, упругость и эластичность. Масло виноградных косточек богато насыщенными жирными кислотами, питает и разглаживает кожу рук. Результат: Гладкая и нежная кожа рук.</description>
            <weight>0.180</weight>
            <country_of_origin>Беларусь</country_of_origin>
            </offer>
            */

        for (Offer element : ymlObject.getBody()) {
            OfferYandex offer = (OfferYandex) element;
            Element blockOffer = getBlockOffer(document, root, offer);
            addStringElement(document, blockOffer, "url", offer::getUrl);
            addDoubleElement(document, blockOffer, "price", offer::getPrice);
            addIntElement(document, blockOffer, "count", offer::getInStock);
            addStringElement(document, blockOffer, "currencyId", offer::getCurrencyId);
            addStringElement(document, blockOffer, "categoryId", offer::getCategoryId);

            offer.getAdditionalPictures().forEach(pic -> addStringElement(document, blockOffer, "picture", pic));

            addBooleanElement(document, blockOffer, "store", offer::getStore);
            addBooleanElement(document, blockOffer, "pickup", offer::getPickup);
            addBooleanElement(document, blockOffer, "delivery", offer::getPickup);
            addStringElement(document, blockOffer, "vendor", offer::getVendor);
            addStringElement(document, blockOffer, "vendorCode", offer::getVendorCode);
            addStringElement(document, blockOffer, "barcode", offer::getBarcode);
            addStringElement(document, blockOffer, "model", offer::getModel);
            addStringElement(document, blockOffer, "description", offer::getDescription);
            addStringElement(document, blockOffer, "dimensions", offer::getDimensions);
            addIntElementWithoutZero(document, blockOffer, "weight", offer::getWeight);
            addStringElement(document, blockOffer, "country_of_origin", offer::getCountry_of_origin);


            //<shipment-options> <option days="1" order-before="15:00"/> </shipment-options>
            Element deliveryOptions = document.createElement("delivery-options");
            blockOffer.appendChild(deliveryOptions);
            Element shipmentOptionElement = document.createElement("option");
            shipmentOptionElement.setAttribute("cost", "1");
            shipmentOptionElement.setAttribute("order-before", "7");
            shipmentOptionElement.setAttribute("days", String.valueOf(offer.getDays()));
            deliveryOptions.appendChild(shipmentOptionElement);
        }
        System.gc();
        return transformBodyToString(document);
    }

    @Override
    public Element getBlockOffer(Document document, Element root, Offer offer) {
        Element blockOffer = document.createElement("offer");
        blockOffer.setAttribute("type", "vendor.model");
        blockOffer.setAttribute("available", String.valueOf(((OfferYandex) offer).isAvailable()));
//        blockOffer.setAttribute("id", String.valueOf(offer.getId()));
        blockOffer.setAttribute("id", String.valueOf(offer.getVendorCode()));
        root.appendChild(blockOffer);
        return blockOffer;
    }

}
