package com.aalifadesigns.matatatraveller.service;

import com.aalifadesigns.matatatraveller.dao.CategoryDao;
import com.aalifadesigns.matatatraveller.dao.ThreadDao;
import com.aalifadesigns.matatatraveller.dao.entities.CategoryEntity;
import com.aalifadesigns.matatatraveller.dao.entities.CityEntity;
import com.aalifadesigns.matatatraveller.dao.entities.ThreadEntity;
import com.aalifadesigns.matatatraveller.exception.ApplicationException;
import com.aalifadesigns.matatatraveller.model.CategoryDto;
import com.aalifadesigns.matatatraveller.model.CityDto;
import com.aalifadesigns.matatatraveller.model.ThreadDto;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ThreadServiceImpl implements ThreadService {

    private ThreadDao threadDao;

    @Autowired
    private CategoryServiceImpl categoryService;
    @Autowired
    private CityServiceImpl cityService;

    @Autowired
    public ThreadServiceImpl(ThreadDao threadDao) {
        this.threadDao = threadDao;
    }

    @Override
    public List<ThreadDto> fetchAllThreads() {
        // Collection to store all the records (which are each in the form of ThreadEtity objects) fetched from the database.
        List<ThreadEntity> allThreadsEntity = threadDao.findAll();
        // Collection of records (which have been stored in Dto objects) to be sent to the controller layer.
        List<ThreadDto> allThreadsDto = new ArrayList<ThreadDto>();

        // Transferring each record from an entity to a dto (including copying the record's composite entity).
        allThreadsEntity.forEach(eachThreadEntity -> {
            ThreadDto eachThreadDto = new ThreadDto();
            // For the composite entity inside the thread entity
            CityDto eachCityDto = new CityDto();
            List<CategoryDto> eachCategoryDtoList = new ArrayList<>();

            // Copy over everything
            BeanUtils.copyProperties(eachThreadEntity, eachThreadDto);
            // Copy over the city composite property
            BeanUtils.copyProperties(eachThreadEntity.getCityEntity(), eachCityDto);
            // Set the city composite property
            eachThreadDto.setCityDto(eachCityDto);
            // Copy over the composite categoryEntities (there may be more than one so we need to do this in a loop)
            eachThreadEntity.getAllCategoriesEntity().forEach(eachCategoryEntity -> {
                CategoryDto eachCategoryDto = new CategoryDto();
                BeanUtils.copyProperties(eachCategoryEntity, eachCategoryDto);
                eachCategoryDtoList.add(eachCategoryDto);
            });
            // Set the categories composite property which is a list of categoryDto
            eachThreadDto.setAllCategoriesDto(eachCategoryDtoList);

            // Add the dto to the collection allThreadsDto
            allThreadsDto.add(eachThreadDto);
        });
        return allThreadsDto;
    }

    @Override
    public ThreadDto fetchAThread(int threadId) {
        // Store the entity returned by the id search in an optional for if id is invalid it is able to hold the value null.
        Optional<ThreadEntity> threadEntityOptional = threadDao.findById(threadId);
        // Store the record, if it exists, in a dto object with its composite entities copied in
        ThreadDto threadDto = new ThreadDto();
        if (threadEntityOptional.isPresent()) {
            CityDto cityDto = new CityDto();        // this is for the composite entity inside the thread entity
            List<CategoryDto> categoryDtoList = new ArrayList<>();

            BeanUtils.copyProperties(threadEntityOptional.get(), threadDto);
            BeanUtils.copyProperties(threadEntityOptional.get().getCityEntity(), cityDto);
            threadDto.setCityDto(cityDto);          // set the composite property

            // Copy over the composite categoryEntities (there may be more than one so we need to do this in a loop)
            threadEntityOptional.get().getAllCategoriesEntity().forEach(eachCategoryEntity -> {
                CategoryDto eachCategoryDto = new CategoryDto();
                BeanUtils.copyProperties(eachCategoryEntity, eachCategoryDto);
                categoryDtoList.add(eachCategoryDto);
            });
            // Set the categories composite property which is a list of categoryDto
            threadDto.setAllCategoriesDto(categoryDtoList);

            return threadDto;
        }
        return null;
    }

    @Override
    public ThreadDto addThread(ThreadDto newThreadDto) {
        // Copy dto into an entity
        ThreadEntity newThreadEntity = new ThreadEntity();
        CityEntity newCityEntity = new CityEntity();
        List<CategoryEntity> categoryEntityList = new ArrayList<>();

        BeanUtils.copyProperties(newThreadDto, newThreadEntity);
        // Copy in the city composite entity too
        BeanUtils.copyProperties(newThreadDto.getCityDto(), newCityEntity);
        newThreadEntity.setCityEntity(newCityEntity);
        // Copy into a list of category entities
        newThreadDto.getAllCategoriesDto().forEach(eachCategoryDao -> {
            CategoryEntity eachCategoryEntity = new CategoryEntity();
            BeanUtils.copyProperties(eachCategoryDao, eachCategoryEntity);
            categoryEntityList.add(eachCategoryEntity);
        });
        // Set the list of categories entities to the newThreadEntity object
        newThreadEntity.setAllCategoriesEntity(categoryEntityList);

        // Add the entity to the database. N.B. if the primary key is not unique, it will update the existing record with this entity
        threadDao.saveAndFlush(newThreadEntity);
        newThreadDto.setThreadId(newThreadEntity.getThreadId());
        return newThreadDto;
    }

    @Override
    public List<ThreadDto> fetchByThreadDate(LocalDate threadDate) {
        // Store the entities returned by the date search in a list of ThreadEntity objects.
        // This list will be empty if there are no threads on the date 'threadDate'
        List<ThreadEntity> allThreadsEntity = threadDao.findByThreadDate(threadDate);
        // Collection of records (which have been stored in Dto objects) to be sent to the controller layer.
        List<ThreadDto> allThreadsDto = new ArrayList<ThreadDto>();

        // Transferring each record from an entity to a dto (including copying the record's composite entity).
        allThreadsEntity.forEach(eachThreadEntity -> {
            ThreadDto eachThreadDto = new ThreadDto();
            CityDto eachCityDto = new CityDto();        // this is for the composite entity inside the thread entity
            List<CategoryDto> eachCategoryDtoList = new ArrayList<>();

            // Copy over everything
            BeanUtils.copyProperties(eachThreadEntity, eachThreadDto);
            // Copy over the city composite property
            BeanUtils.copyProperties(eachThreadEntity.getCityEntity(), eachCityDto);
            // Set the composite property
            eachThreadDto.setCityDto(eachCityDto);
            // Copy over and set the composite category list
            eachThreadEntity.getAllCategoriesEntity().forEach(eachCategoryEntity -> {
                CategoryDto eachCategoryDto = new CategoryDto();
                BeanUtils.copyProperties(eachCategoryEntity, eachCategoryDto);
                eachCategoryDtoList.add(eachCategoryDto);
            });
            // Set the categories composite property which is a list of categoryDto
            eachThreadDto.setAllCategoriesDto(eachCategoryDtoList);

            // Add the dto to the collection allThreadsDto
            allThreadsDto.add(eachThreadDto);
        });
        return allThreadsDto;
    }

    @Override
    public ThreadDto updateThread(ThreadDto updateThreadDto) {
        ThreadEntity updateThreadEntity = new ThreadEntity();
        CityEntity updateCityEntity = new CityEntity();
        List<CategoryEntity> updateCategoryEntityList = new ArrayList<>();

        BeanUtils.copyProperties(updateThreadDto, updateThreadEntity);
        // Copy and set the city composite entity too
        BeanUtils.copyProperties(updateThreadDto.getCityDto(), updateCityEntity);
        updateThreadEntity.setCityEntity(updateCityEntity);
        // Copy and set the category list from the entity to the dao
        updateThreadDto.getAllCategoriesDto().forEach(eachCategoryDao -> {
            CategoryEntity eachCategoryEntity = new CategoryEntity();
            BeanUtils.copyProperties(eachCategoryDao, eachCategoryEntity);
            updateCategoryEntityList.add(eachCategoryEntity);
        });
        // Set the list of categories entities to the updateThreadEntity object
        updateThreadEntity.setAllCategoriesEntity(updateCategoryEntityList);

        // update the entity in the database. N.B. if the primary key is unique, it will add a new record with this entity
        threadDao.saveAndFlush(updateThreadEntity);
        updateThreadDto.setThreadId(updateThreadEntity.getThreadId());
        return updateThreadDto;
    }

    @Override
    public void deleteThread(int threadId) {
        Optional<ThreadEntity> deleteThreadEntityOptional = threadDao.findById(threadId);
        if (deleteThreadEntityOptional.isPresent()) {
            threadDao.deleteById(threadId);
        } else {
            // EDIT: add a custom exception here.
            System.out.println("Error. Invalid thread id.");
        }
    }

    @Override
    public List<ThreadDto> fetchThreadsByCity(int cityId) {

        //fetch CityDto object using cityService, passing the cityId (used path variable) as parameter
        CityDto cityDto = cityService.fetchACity(cityId);

        //if the city does not exist throw custom exception referring to DataAccess
        if (cityDto == null) {
            throw new ApplicationException();
        }

        //copy the CityDto into a city entity
        CityEntity cityEntity = new CityEntity();
        BeanUtils.copyProperties(cityDto, cityEntity);

        //call the finder method declared in the ThreadDao
        List<ThreadEntity> allThreadEntity = threadDao.findByCityEntity(cityEntity);

        //the ThreadEntity collection will need to be copied into a DTO collection
        List<ThreadDto> allThreadDto = new ArrayList<ThreadDto>();

        // traverse the collection and copy each entity into a DTO, also copying the categories collections
        allThreadEntity.forEach(eachThreadEntity -> {
            ThreadDto eachThreadDto = new ThreadDto();
            List<CategoryDto> allCategories = new ArrayList<>();
            BeanUtils.copyProperties(eachThreadEntity, eachThreadDto);

            // traverse the Categories entities collection, copy each category entity into a CategoryDto object
            // then add the CategoryDto object to a CategoryDto collection, which will be set inside each ThreadDto
            eachThreadEntity.getAllCategoriesEntity().forEach(eachCategoryEntity -> {
                CategoryDto eachCategoryDto = new CategoryDto();
                BeanUtils.copyProperties(eachCategoryEntity, eachCategoryDto);

                //add each category to the allCategories collection
                allCategories.add(eachCategoryDto);
            });

            // set the Categories collection list
            eachThreadDto.setAllCategoriesDto(allCategories);

            // add each threadDto object (containing the categories) to the collection of ThreadDto
            allThreadDto.add(eachThreadDto);
        });
        //return the collection
        return allThreadDto;
    }

    @Override
    public List<ThreadDto> fetchThreadsByCategory(int categoryId) {

        //fetch CategoryDto object, passing the categoryId (cid - used path variable) as parameter
        CategoryDto categoryDto = categoryService.fetchACategory(categoryId);
        //if the category does not exist throw custom exception referring to DataAccess
        if (categoryDto == null) {
            throw new ApplicationException();
        }

        //copy the DTO into a category entity
        CategoryEntity categoryEntity = new CategoryEntity();
        BeanUtils.copyProperties(categoryDto, categoryEntity);

        //call the finder method declared in the ThreadDao
        List<ThreadEntity> allThreadEntity = threadDao.findByAllCategoriesEntity(categoryEntity);

        //the ThreadEntity collection will need to be copied into a DTO collection
        List<ThreadDto> allThreadDto = new ArrayList<ThreadDto>();

        // traverse the collection and copy each entity into a DTO, also copying the categories collections
        allThreadEntity.forEach(eachThreadEntity -> {
            ThreadDto eachThreadDto = new ThreadDto();
            List<CategoryDto> allCategories = new ArrayList<>();
            BeanUtils.copyProperties(eachThreadEntity, eachThreadDto);

            // traverse the Categories entities collection, copy each category entity into a CategoryDto object
            // then add the CategoryDto object to a CategoryDto collection, which will be set inside each ThreadDto
            eachThreadEntity.getAllCategoriesEntity().forEach(eachCategoryEntity -> {
                CategoryDto eachCategoryDto = new CategoryDto();
                BeanUtils.copyProperties(eachCategoryEntity, eachCategoryDto);

                //add each category to the allCategories collection
                allCategories.add(eachCategoryDto);
            });

            // set the Categories collection list
            eachThreadDto.setAllCategoriesDto(allCategories);

            //also copy the City entity / DTO
            CityDto cityDto = new CityDto();
            BeanUtils.copyProperties(eachThreadEntity.getCityEntity(), cityDto);

            //set the CityDto
            eachThreadDto.setCityDto(cityDto);

            // add each threadDto object (containing the categories collection and the city object) to the collection
            allThreadDto.add(eachThreadDto);
        });

        return allThreadDto;
    }

    @Override
    public List<ThreadDto> fetchThreadsByCityAndCategory(int cityId, int categoryId) {

        //call the methods which return the ThreadDto collections and add the common objects into a new Threads collection
        List<ThreadDto> allThreadsByCity = fetchThreadsByCity(cityId);
        List<ThreadDto> allThreadsByCategory = fetchThreadsByCategory(categoryId);

        // traverse one collection and check if the ThreadDto is also present in the other collection
        // if yes, add the Thread item to the new Thread collection (allThreadsByCityAndCategory)
        List<ThreadDto> allThreadsByCityAndCategory = new ArrayList<ThreadDto>();
        List<ThreadEntity> allThreadsByCityAndCategoryEntity = new ArrayList<ThreadEntity>();
        for (ThreadDto thread : allThreadsByCity) {
            if (allThreadsByCategory.contains(thread)) {
                allThreadsByCityAndCategory.add(thread);
            }
        }

        //return the Threads collection
        return allThreadsByCityAndCategory;
    }
}