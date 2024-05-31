package learn.foraging.domain;

import learn.foraging.data.DataException;
import learn.foraging.data.ForagerRepository;
import learn.foraging.models.Forager;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Collectors;


public class ForagerService {

    private static  ForagerRepository repository;

    public ForagerService(ForagerRepository repository) {
        this.repository = repository;
    }

    public List<Forager> findByState(String stateAbbr) {
        return repository.findByState(stateAbbr);
    }

    public List<Forager> findByLastName(String prefix) {
        return repository.findAll().stream()
                .filter(i -> i.getLastName().startsWith(prefix))
                .collect(Collectors.toList());
    }

    public static Result<Forager> add(Forager forager) throws DataException, FileNotFoundException {
        Result<Forager> result = validate(forager);

        if (!result.isSuccess()) {
            return result;
        }

        forager = repository.add(forager);
        result.setPayload(forager);
        return result;
    }

    private static Result<Forager> validate(Forager forager) {
        Result<Forager> result = new Result<>();

        if (forager.getFirstName() == null || forager.getFirstName().isEmpty()) {
            result.addErrorMessage("First name is required.");
        }

        if (forager.getLastName() == null || forager.getLastName().isEmpty()) {
            result.addErrorMessage("Last name is required.");
        }

        if (forager.getState() == null || forager.getState().isEmpty()) {
            result.addErrorMessage("State is required.");
        }

        List<Forager> foragerInState = repository.findByState(forager.getState());

        if (foragerInState.stream().anyMatch(existingPanel ->
                existingPanel.getFirstName().equals(forager.getFirstName()) &&
                        existingPanel.getLastName().equals(forager.getLastName()))) {

            result.addErrorMessage("Duplicate forager found.");
        }

        return result;
    }
}
