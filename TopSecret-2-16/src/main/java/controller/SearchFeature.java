package controller;

import models.Mission;

import java.util.List;

/**
 * SearchFeature interface uses search functionality
 * for mission briefs in the database.
 * Any class implementation must provide
 * a method for mission brief searching by word or phrase.
 */
public interface SearchFeature
{
    /**
     * Searches mission briefs for a given phrase.
     * Search should ignoreCase, return all matching missions
     *
     * @param phrase the word or phrase to search for
     * @return a list of missions containing the phrase in the brief
     */
    List<Mission> searchMissions(String phrase);
}