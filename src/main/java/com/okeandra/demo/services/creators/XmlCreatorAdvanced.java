package com.okeandra.demo.services.creators;

import com.okeandra.demo.models.Offer;
import com.okeandra.demo.models.YmlObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Component
@Qualifier("xmlAdvancedCreator")
public class XmlCreatorAdvanced extends XmlCreator {
    @Override
    public String getBodyAsXML(YmlObject ymlObject) {
        Document document = getDocument(ymlObject);
        Element root = document.createElement("offers");
        document.appendChild(root);

        for (Offer offer : ymlObject.getBody()) {
            Element blockOffer = getBlockOffer(document, root, offer);
            addStringElement(document, blockOffer, "url", offer::getUrl);
            addDoubleElement(document, blockOffer, "price", offer::getPrice);
            addStringElement(document, blockOffer, "categoryId", offer::getCategoryId);
            addStringElement(document, blockOffer, "picture", offer::getPicture);
            addStringElement(document, blockOffer, "name", offer::getName);
            addStringElement(document, blockOffer, "vendor", offer::getVendor);
            addStringElement(document, blockOffer, "vendorCode", offer::getVendorCode);
            addStringElement(document, blockOffer, "barcode", offer::getBarcode);
            addStringElement(document, blockOffer, "description", offer::getDescription);
            addStringElement(document, blockOffer, "main-category", offer::getRootCategory);
            addStringElement(document, blockOffer, "destination", offer::getNaznachenie);
            addStringElement(document, blockOffer, "kind-product", offer::getVidProduc);
            addIntElementWithoutZero(document, blockOffer, "weight", offer::getWeight);
            addStringFixedElement(document, blockOffer, "vat", "NO_VAT");

            Element outlets = document.createElement("outlets");
            blockOffer.appendChild(outlets);
            Element outlet = document.createElement("outlet");
            outlet.setAttribute("id", offer.getOutletId());
            outlet.setAttribute("instock", String.valueOf(offer.getInStock()));
            outlets.appendChild(outlet);

            Element shipmentOptions = document.createElement("shipment-options");
            blockOffer.appendChild(shipmentOptions);
            Element shipmentOptionElement = document.createElement("option");
            shipmentOptionElement.setAttribute("order-before", "07:00");
            if (offer.getDays() == null) {
                shipmentOptionElement.setAttribute("days", String.valueOf(1));
            } else {
                shipmentOptionElement.setAttribute("days", String.valueOf(offer.getDays()));
            }
            shipmentOptions.appendChild(shipmentOptionElement);
        }

        return transformBodyToString(document);
    }
}
