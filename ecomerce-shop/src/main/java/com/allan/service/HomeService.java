package com.allan.service;

import java.util.List;

import com.allan.model.Home;
import com.allan.model.HomeCategory;

public interface HomeService {

    public Home createHomePageData(List<HomeCategory> allCategories);

}
