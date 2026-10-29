import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Pagination } from './pagination';

describe('Pagination', () => {
  let fixture: ComponentFixture<Pagination>;
  let element: HTMLElement;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [Pagination], providers: [provideRouter([])] });
    fixture = TestBed.createComponent(Pagination);
    element = fixture.nativeElement;
  });

  async function render(currentPage: number, totalPages: number): Promise<string[]> {
    fixture.componentRef.setInput('currentPage', currentPage);
    fixture.componentRef.setInput('totalPages', totalPages);
    await fixture.whenStable();
    return Array.from(element.querySelectorAll('.page-link, .ellipsis')).map((item) =>
      item.textContent!.trim(),
    );
  }

  it("ne s'affiche pas s'il n'y a qu'une page", async () => {
    expect(await render(1, 1)).toEqual([]);
  });

  it('affiche la première page comme sur la maquette', async () => {
    expect(await render(1, 45)).toEqual(['1', '2', '…', '45', '›']);
  });

  it('affiche les pages autour de la page courante', async () => {
    expect(await render(10, 45)).toEqual(['‹', '1', '…', '9', '10', '11', '…', '45', '›']);
  });

  it('pointe vers la bonne page', async () => {
    await render(1, 3);

    const next = element.querySelector<HTMLAnchorElement>('[aria-label="Page suivante"]')!;
    expect(next.getAttribute('href')).toBe('/?page=2');
  });
});
