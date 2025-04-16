package com.psu.rouen.cphbox.security;

public class OrderConstants {

    public static final String ORDER_NEW = "NOUVELLE_COMMANDE";
    public static final String ORDER_IN_PREPARATION = "PREPARATION_EN_COURS";

    public static final String ORDER_PENDING = "COMMANDE_EN_ATTENTE";
    public static final String ORDER_VALIDDATED = "COMMANDE_VALIDE";
    public static final String ORDER_INPROGRESS = "COMMANDE_EN_COURS";

    public static final String ORDER_TO_SHIP = "COMMANDE_EN_COURS";

    public static final String ORDER_RETURN = "COMMANDE_EN_COURS";

    public static final String ORDER_DONE = "COMMANDE_TRAITER";


    public static final String ORDERITEM_INPROGRESS = "ORDERITEM_SELECTIONNER";
    public static final String ORDERITEM_RECOVER = "ARTICLE_TRAITER";
    public static final String ORDERITEM_NO_RECOVER = "ARTICLE_NON_TRAITER";
    public static final String ORDERITEM_NOT_FOUND = "ARTICLE_HORS_CARTON";

    public static final String ORDER_DONE_COMMENT = "PAS DE COMMENTAIRE";
    public static final String ORDERITEM_INSUFFICIENT = "ARTICLE_INSUFFISANT_CARTON";


    private OrderConstants() {}
}
