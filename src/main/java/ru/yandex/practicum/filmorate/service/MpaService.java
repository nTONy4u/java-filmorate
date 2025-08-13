package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.data.dao.MpaDao;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

@Service
public class MpaService {
    private final MpaDao mpaDao;

    @Autowired
    public MpaService(@Qualifier("MpaDao") MpaDao mpaDao) {
        this.mpaDao = mpaDao;
    }

    public List<MpaRating> getAllMpaRatings() {
        return mpaDao.getAllMpaRatings();
    }

    public MpaRating getMpaById(int id) {
        return mpaDao.getMpaById(id);
    }
}