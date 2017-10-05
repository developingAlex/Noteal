# Noteal
Android app for taking simple notes.

## Inspiration / Motivation
Android doesn't automatically come with a note taking app so I decided to try my hand at making my own.

## How to use
### Creating a new note
1. Click the top right 'kebab' menu to access the option to create a new note.
1. When the new note loads its title and contents will be blank type something in both to have a valid note to save.

### Saving a note or changes to a note
**(A note will not save without a title and some content)**
There are two ways to save a note:
- Click the top right 'kebab' menu to access the option to Save
- Use your phones Back navigation button to return to the list of notes, the changes will be automatically saved.

### Deleting a note
Deleting a note can be done from the main list of notes view
1. Long press on the note you want to delete.
1. Select yes to the confirmation message.

### Exporting notes
1. Click on the note you want to export so that you're viewing its contents
1. Click on the top right 'kebab' menu.
1. Select the option to export the note.
1. A notification will popup showing you the path to your note, a note will be available from the *noteal* directory in your SDCards root directory.
  - If you had a title of *shopping* for a note you exported, then you should be able to find it at /noteal/noteal-shopping.txt

## Issues to resolve

- When deleting a note the confirmation doesn't specify which note is being deleted
- When the contents of a note become very long, and a cursor is active in the content with the onscreen keyboard visible, sometimes it is impossible to scroll by swiping to see the very bottom of the content because its hidden behind the keyboard. A workaround is to 'drag' the cursor (in a similar manner to when highlighting text to copy) and drag it all the way to the bottom of the file.
- When the contents of a note become more than a screen's worth, attempts to copy parts of the text by long pressing to highlight can sometimes result in the on screen keyboard sliding up from the bottom, which inadvertantly pushes the top bar of the view being pushed up and out of the view and as such you lose the access to the buttons of copy, cut and paste.
