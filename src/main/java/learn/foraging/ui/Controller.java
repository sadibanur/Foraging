package learn.foraging.ui;

import learn.foraging.data.DataException;
import learn.foraging.domain.ForageService;
import learn.foraging.domain.ForagerService;
import learn.foraging.domain.ItemService;
import learn.foraging.domain.Result;
import learn.foraging.models.Category;
import learn.foraging.models.Forage;
import learn.foraging.models.Forager;
import learn.foraging.models.Item;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller {

    private final ForagerService foragerService;
    private final ForageService forageService;
    private final ItemService itemService;
    private final View view;

    public Controller(ForagerService foragerService, ForageService forageService, ItemService itemService, View view) {
        this.foragerService = foragerService;
        this.forageService = forageService;
        this.itemService = itemService;
        this.view = view;
    }

    public void run() {
        view.displayHeader("Welcome to Sustainable Foraging");
        try {
            runAppLoop();
        } catch (DataException | FileNotFoundException ex) {
            view.displayException(ex);
        }
        view.displayHeader("Goodbye.");
    }

    private void runAppLoop() throws DataException, FileNotFoundException {
        MainMenuOption option;
        do {
            option = view.selectMainMenuOption();
            switch (option) {
                case VIEW_FORAGES_BY_DATE:
                    viewByDate();
                    break;
                case VIEW_ITEMS:
                    viewItems();
                    break;
                case ADD_FORAGE:
                    addForage();
                    break;
                case ADD_FORAGER:
                    view.displayStatus(false, "NOT IMPLEMENTED");
                    addForager();
                    view.enterToContinue();
                    break;
                case ADD_ITEM:
                    addItem();
                    break;
                case REPORT_KG_PER_ITEM:
                    view.displayStatus(false, "NOT IMPLEMENTED");
                    reportKgPerItem();
                    view.enterToContinue();
                    break;
                case REPORT_CATEGORY_VALUE:
                    view.displayStatus(false, "NOT IMPLEMENTED");
                    reportCategoryValue();
                    view.enterToContinue();
                    break;
                case GENERATE:
                    generate();
                    break;
            }
        } while (option != MainMenuOption.EXIT);
    }

    // top level menu
    private void viewByDate() {
        LocalDate date = view.getForageDate();
        List<Forage> forages = forageService.findByDate(date);
        view.displayForages(forages);
        view.enterToContinue();
    }

    private void viewItems() {
        view.displayHeader(MainMenuOption.VIEW_ITEMS.getMessage());
        Category category = view.getItemCategory();
        List<Item> items = itemService.findByCategory(category);
        view.displayHeader("Items");
        view.displayItems(items);
        view.enterToContinue();
    }

    private void addForage() throws DataException {
        view.displayHeader(MainMenuOption.ADD_FORAGE.getMessage());
        Forager forager = getForager();
        if (forager == null) {
            return;
        }
        Item item = getItem();
        if (item == null) {
            return;
        }
        Forage forage = view.makeForage(forager, item);
        Result<Forage> result = forageService.add(forage);
        if (!result.isSuccess()) {
            view.displayStatus(false, result.getErrorMessages());
        } else {
            String successMessage = String.format("Forage %s created.", result.getPayload().getId());
            view.displayStatus(true, successMessage);
        }
    }


    private void addForager() throws DataException, FileNotFoundException {
        Forager forager = view.makeForager();

        Result<Forager> result = ForagerService.add(forager);

        if (result.isSuccess()) {
            System.out.println("[Success]");
            System.out.printf("Forager '%s %s' added.", forager.getFirstName(),
                    forager.getLastName());
        } else {
            System.out.println(result.getErrorMessages().get(0));
        }

    }

    private void addItem() throws DataException {
        Item item = view.makeItem();
        Result<Item> result = itemService.add(item);
        if (!result.isSuccess()) {
            view.displayStatus(false, result.getErrorMessages());
        } else {
            String successMessage = String.format("Item %s created.", result.getPayload().getId());
            view.displayStatus(true, successMessage);
        }
    }

    private void generate() throws DataException {
        GenerateRequest request = view.getGenerateRequest();
        if (request != null) {
            int count = forageService.generate(request.getStart(), request.getEnd(), request.getCount());
            view.displayStatus(true, String.format("%s forages generated.", count));
        }
    }

    // support methods
    private Forager getForager() {
        String lastNamePrefix = view.getForagerNamePrefix();
        List<Forager> foragers = foragerService.findByLastName(lastNamePrefix);
        return view.chooseForager(foragers);
    }

    private Item getItem() {
        Category category = view.getItemCategory();
        List<Item> items = itemService.findByCategory(category);
        return view.chooseItem(items);
    }

    private void reportKgPerItem() {
        LocalDate date = view.getForageDate();
        Map<Item, BigDecimal> kgByItem = calculateKgByItem(date);

        view.displayHeader("Kilograms of Item for the Date " + date);

        for (Map.Entry<Item, BigDecimal> entry : kgByItem.entrySet()) {
            view.displayKgItem(entry.getKey(), entry.getValue());
        }
    }

    private void reportCategoryValue() {
        LocalDate date = view.getForageDate();
        Map<Category, BigDecimal> categoryValue = calculateCategoryValuesByDate(date);

        view.displayHeader("Item Category Value for date " + date);

        view.displayCategoryValue(categoryValue);

    }


    private Map<Item, BigDecimal> calculateKgByItem(LocalDate date) {
        List<Forage> forages = forageService.findByDate(date);
        Map<Item, BigDecimal> kgByItem = new HashMap<>();
        for (Forage forage : forages) {
            Item item = forage.getItem();
            BigDecimal kg = kgByItem.getOrDefault(item, BigDecimal.ZERO);
            kg = kg.add(BigDecimal.valueOf(forage.getKilograms()));
            kgByItem.put(item, kg);
        }
        return kgByItem;
    }


    private Map<Category, BigDecimal> calculateCategoryValuesByDate(LocalDate date) {
        List<Forage> forages = forageService.findByDate(date);
        Map<Category, BigDecimal> categoryValue = new HashMap<>();
        for (Forage forage : forages) {
            Item item = forage.getItem();
            BigDecimal value = item.getDollarPerKilogram().multiply(
                    BigDecimal.valueOf(forage.getKilograms()));
            categoryValue.merge(item.getCategory(), value, BigDecimal::add);
        }
        return categoryValue;
    }

}
