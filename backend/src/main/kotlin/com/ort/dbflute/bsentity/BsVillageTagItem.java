package com.ort.dbflute.bsentity;

import java.util.List;
import java.util.ArrayList;

import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.AbstractEntity;
import org.dbflute.dbmeta.accessory.DomainEntity;
import com.ort.dbflute.allcommon.DBMetaInstanceHandler;
import com.ort.dbflute.exentity.*;

/**
 * The entity of VILLAGE_TAG_ITEM as TABLE. <br>
 * 村タグ種別
 * <pre>
 * [primary-key]
 *     VILLAGE_TAG_ITEM_CODE
 *
 * [column]
 *     VILLAGE_TAG_ITEM_CODE, VILLAGE_TAG_ITEM_NAME, DISP_ORDER
 *
 * [sequence]
 *     
 *
 * [identity]
 *     
 *
 * [version-no]
 *     
 *
 * [foreign table]
 *     
 *
 * [referrer table]
 *     VILLAGE_TAG
 *
 * [foreign property]
 *     
 *
 * [referrer property]
 *     villageTagList
 *
 * [get/set template]
 * /= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
 * String villageTagItemCode = entity.getVillageTagItemCode();
 * String villageTagItemName = entity.getVillageTagItemName();
 * Integer dispOrder = entity.getDispOrder();
 * entity.setVillageTagItemCode(villageTagItemCode);
 * entity.setVillageTagItemName(villageTagItemName);
 * entity.setDispOrder(dispOrder);
 * = = = = = = = = = =/
 * </pre>
 * @author DBFlute(AutoGenerator)
 */
public abstract class BsVillageTagItem extends AbstractEntity implements DomainEntity {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** VILLAGE_TAG_ITEM_CODE: {PK, NotNull, VARCHAR(50)} */
    protected String _villageTagItemCode;

    /** VILLAGE_TAG_ITEM_NAME: {NotNull, VARCHAR(50)} */
    protected String _villageTagItemName;

    /** DISP_ORDER: {NotNull, INT UNSIGNED(10)} */
    protected Integer _dispOrder;

    // ===================================================================================
    //                                                                             DB Meta
    //                                                                             =======
    /** {@inheritDoc} */
    public DBMeta asDBMeta() {
        return DBMetaInstanceHandler.findDBMeta(asTableDbName());
    }

    /** {@inheritDoc} */
    public String asTableDbName() {
        return "village_tag_item";
    }

    // ===================================================================================
    //                                                                        Key Handling
    //                                                                        ============
    /** {@inheritDoc} */
    public boolean hasPrimaryKeyValue() {
        if (_villageTagItemCode == null) { return false; }
        return true;
    }

    // ===================================================================================
    //                                                                    Foreign Property
    //                                                                    ================
    // ===================================================================================
    //                                                                   Referrer Property
    //                                                                   =================
    /** VILLAGE_TAG by VILLAGE_TAG_ITEM_CODE, named 'villageTagList'. */
    protected List<VillageTag> _villageTagList;

    /**
     * [get] VILLAGE_TAG by VILLAGE_TAG_ITEM_CODE, named 'villageTagList'.
     * @return The entity list of referrer property 'villageTagList'. (NotNull: even if no loading, returns empty list)
     */
    public List<VillageTag> getVillageTagList() {
        if (_villageTagList == null) { _villageTagList = newReferrerList(); }
        return _villageTagList;
    }

    /**
     * [set] VILLAGE_TAG by VILLAGE_TAG_ITEM_CODE, named 'villageTagList'.
     * @param villageTagList The entity list of referrer property 'villageTagList'. (NullAllowed)
     */
    public void setVillageTagList(List<VillageTag> villageTagList) {
        _villageTagList = villageTagList;
    }

    protected <ELEMENT> List<ELEMENT> newReferrerList() { // overriding to import
        return new ArrayList<ELEMENT>();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    protected boolean doEquals(Object obj) {
        if (obj instanceof BsVillageTagItem) {
            BsVillageTagItem other = (BsVillageTagItem)obj;
            if (!xSV(_villageTagItemCode, other._villageTagItemCode)) { return false; }
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected int doHashCode(int initial) {
        int hs = initial;
        hs = xCH(hs, asTableDbName());
        hs = xCH(hs, _villageTagItemCode);
        return hs;
    }

    @Override
    protected String doBuildStringWithRelation(String li) {
        StringBuilder sb = new StringBuilder();
        if (_villageTagList != null) { for (VillageTag et : _villageTagList)
        { if (et != null) { sb.append(li).append(xbRDS(et, "villageTagList")); } } }
        return sb.toString();
    }

    @Override
    protected String doBuildColumnString(String dm) {
        StringBuilder sb = new StringBuilder();
        sb.append(dm).append(xfND(_villageTagItemCode));
        sb.append(dm).append(xfND(_villageTagItemName));
        sb.append(dm).append(xfND(_dispOrder));
        if (sb.length() > dm.length()) {
            sb.delete(0, dm.length());
        }
        sb.insert(0, "{").append("}");
        return sb.toString();
    }

    @Override
    protected String doBuildRelationString(String dm) {
        StringBuilder sb = new StringBuilder();
        if (_villageTagList != null && !_villageTagList.isEmpty())
        { sb.append(dm).append("villageTagList"); }
        if (sb.length() > dm.length()) {
            sb.delete(0, dm.length()).insert(0, "(").append(")");
        }
        return sb.toString();
    }

    @Override
    public VillageTagItem clone() {
        return (VillageTagItem)super.clone();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * [get] VILLAGE_TAG_ITEM_CODE: {PK, NotNull, VARCHAR(50)} <br>
     * 村タグ種別コード
     * @return The value of the column 'VILLAGE_TAG_ITEM_CODE'. (basically NotNull if selected: for the constraint)
     */
    public String getVillageTagItemCode() {
        checkSpecifiedProperty("villageTagItemCode");
        return convertEmptyToNull(_villageTagItemCode);
    }

    /**
     * [set] VILLAGE_TAG_ITEM_CODE: {PK, NotNull, VARCHAR(50)} <br>
     * 村タグ種別コード
     * @param villageTagItemCode The value of the column 'VILLAGE_TAG_ITEM_CODE'. (basically NotNull if update: for the constraint)
     */
    public void setVillageTagItemCode(String villageTagItemCode) {
        registerModifiedProperty("villageTagItemCode");
        _villageTagItemCode = villageTagItemCode;
    }

    /**
     * [get] VILLAGE_TAG_ITEM_NAME: {NotNull, VARCHAR(50)} <br>
     * 村タグ種別名
     * @return The value of the column 'VILLAGE_TAG_ITEM_NAME'. (basically NotNull if selected: for the constraint)
     */
    public String getVillageTagItemName() {
        checkSpecifiedProperty("villageTagItemName");
        return convertEmptyToNull(_villageTagItemName);
    }

    /**
     * [set] VILLAGE_TAG_ITEM_NAME: {NotNull, VARCHAR(50)} <br>
     * 村タグ種別名
     * @param villageTagItemName The value of the column 'VILLAGE_TAG_ITEM_NAME'. (basically NotNull if update: for the constraint)
     */
    public void setVillageTagItemName(String villageTagItemName) {
        registerModifiedProperty("villageTagItemName");
        _villageTagItemName = villageTagItemName;
    }

    /**
     * [get] DISP_ORDER: {NotNull, INT UNSIGNED(10)} <br>
     * 表示順
     * @return The value of the column 'DISP_ORDER'. (basically NotNull if selected: for the constraint)
     */
    public Integer getDispOrder() {
        checkSpecifiedProperty("dispOrder");
        return _dispOrder;
    }

    /**
     * [set] DISP_ORDER: {NotNull, INT UNSIGNED(10)} <br>
     * 表示順
     * @param dispOrder The value of the column 'DISP_ORDER'. (basically NotNull if update: for the constraint)
     */
    public void setDispOrder(Integer dispOrder) {
        registerModifiedProperty("dispOrder");
        _dispOrder = dispOrder;
    }
}
