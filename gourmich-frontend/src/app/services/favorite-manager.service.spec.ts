import { TestBed } from '@angular/core/testing';

import { FavoriteManagerService } from './favorite-manager.service';

describe('FavoriteManagerService', () => {
  let service: FavoriteManagerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FavoriteManagerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
