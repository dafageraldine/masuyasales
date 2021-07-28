package com.yusuffahrudin.masuyamobileapp.data;

/**
 * Created by yusuf fahrudin on 22-04-2017.
 */

public class UserAkses {
    private String level, modul;
    private int akses, add, edit, delete, post;

    public UserAkses(){  }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getModul() {
        return modul;
    }

    public void setModul(String modul) {
        this.modul = modul;
    }

    public int getAkses() { return akses; }

    public void setAkses(int akses) { this.akses = akses; }

    public int getAdd() { return add; }

    public void setAdd(int add) { this.add = add; }

    public int getEdit() { return edit; }

    public void setEdit(int edit) { this.edit = edit; }

    public int getDelete() { return delete; }

    public void setDelete(int delete) { this.delete = delete; }

    public int getPost() { return post; }

    public void setPost(int post) { this.post = post; }
}
