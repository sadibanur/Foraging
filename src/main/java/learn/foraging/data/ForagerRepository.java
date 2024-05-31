package learn.foraging.data;

import learn.foraging.models.Forager;

import java.io.FileNotFoundException;
import java.util.List;

public interface ForagerRepository {
    Forager findById(String id);

    List<Forager> findAll();

    List<Forager> findByState(String stateAbbr);

    Forager add(Forager forager) throws DataException, FileNotFoundException;
}
