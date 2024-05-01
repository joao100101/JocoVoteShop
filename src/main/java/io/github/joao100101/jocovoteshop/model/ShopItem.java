package io.github.joao100101.jocovoteshop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopItem {
    private Integer slot;
    private String item;
    private String name;
    private boolean purchasable;
    private boolean command;
    private String commandLine;
    private Integer value;
    private List<String> enchantments = new ArrayList<>();
    private List<String> lore = new ArrayList<>();

}
