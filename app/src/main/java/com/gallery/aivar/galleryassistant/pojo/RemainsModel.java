package com.gallery.aivar.galleryassistant.pojo;

public class RemainsModel {

    private String party;
    private String remain;

    public RemainsModel(String party, String remain) {
        this.party = party;
        this.remain = remain;
    }

    public String getParty() {
        return party;
    }

    public void setParty(String party) {
        this.party = party;
    }

    public String getRemain() {
        return remain;
    }

    public void setRemain(String remain) {
        this.remain = remain;
    }
}
