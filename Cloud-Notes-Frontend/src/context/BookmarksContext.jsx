import React, { createContext, useContext, useState, useEffect } from 'react';

const BookmarksContext = createContext();

const STORAGE_KEY = 'cloudnotes_bookmarks';

export const BookmarksProvider = ({ children }) => {
  const [bookmarks, setBookmarks] = useState(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      return saved ? JSON.parse(saved) : { notes: [], papers: [] };
    } catch (e) {
      console.error('Failed to parse bookmarks', e);
      return { notes: [], papers: [] };
    }
  });

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(bookmarks));
    } catch (e) {
      console.error('Failed to save bookmarks', e);
    }
  }, [bookmarks]);

  const isBookmarked = (id, type = 'notes') => {
    const list = bookmarks[type] || [];
    return list.some((item) => (typeof item === 'string' ? item === id : item.id === id));
  };

  const toggleBookmark = (item, type = 'notes') => {
    setBookmarks((prev) => {
      const list = prev[type] || [];
      const itemId = typeof item === 'string' ? item : item.id;
      const exists = list.some((i) => (typeof i === 'string' ? i === itemId : i.id === itemId));

      const updatedList = exists
        ? list.filter((i) => (typeof i === 'string' ? i !== itemId : i.id !== itemId))
        : [...list, item];

      return {
        ...prev,
        [type]: updatedList
      };
    });
  };

  const clearBookmarks = (type = null) => {
    if (type) {
      setBookmarks((prev) => ({ ...prev, [type]: [] }));
    } else {
      setBookmarks({ notes: [], papers: [] });
    }
  };

  const totalBookmarks = (bookmarks.notes?.length || 0) + (bookmarks.papers?.length || 0);

  return (
    <BookmarksContext.Provider
      value={{
        bookmarks,
        isBookmarked,
        toggleBookmark,
        clearBookmarks,
        totalBookmarks,
        bookmarkedNotes: bookmarks.notes || [],
        bookmarkedPapers: bookmarks.papers || []
      }}
    >
      {children}
    </BookmarksContext.Provider>
  );
};

export const useBookmarks = () => {
  const context = useContext(BookmarksContext);
  if (!context) {
    throw new Error('useBookmarks must be used within a BookmarksProvider');
  }
  return context;
};
